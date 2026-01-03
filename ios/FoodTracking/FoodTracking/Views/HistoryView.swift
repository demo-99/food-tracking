// HistoryView - View past food entries

import SwiftUI
import SwiftData

struct HistoryView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \FoodEntry.date, order: .descending) private var allEntries: [FoodEntry]
    
    private var groupedEntries: [(Date, [FoodEntry])] {
        Dictionary(grouping: allEntries) { entry in
            Calendar.current.startOfDay(for: entry.date)
        }
        .sorted { $0.key > $1.key }
        .map { ($0.key, $0.value.sorted { $0.createdAt > $1.createdAt }) }
    }
    
    var body: some View {
        NavigationStack {
            Group {
                if allEntries.isEmpty {
                    emptyState
                } else {
                    List {
                        ForEach(groupedEntries, id: \.0) { date, entries in
                            Section {
                                ForEach(entries) { entry in
                                    HistoryEntryRow(entry: entry)
                                        .swipeActions(edge: .trailing) {
                                            Button(role: .destructive) {
                                                deleteEntry(entry)
                                            } label: {
                                                Label("Delete", systemImage: "trash")
                                            }
                                        }
                                }
                            } header: {
                                HStack {
                                    Text(formatDate(date))
                                        .font(.headline)
                                    
                                    Spacer()
                                    
                                    let dayCalories = entries.reduce(0) { $0 + $1.calories }
                                    Text("\(dayCalories) kcal")
                                        .font(.subheadline)
                                        .foregroundStyle(.orange)
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("History")
        }
    }
    
    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "clock.arrow.circlepath")
                .font(.system(size: 60))
                .foregroundStyle(.secondary)
            
            Text("No history yet")
                .font(.title3)
                .fontWeight(.medium)
            
            Text("Your food entries will appear here")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
    }
    
    private func formatDate(_ date: Date) -> String {
        let calendar = Calendar.current
        
        if calendar.isDateInToday(date) {
            return "Today"
        } else if calendar.isDateInYesterday(date) {
            return "Yesterday"
        } else {
            let formatter = DateFormatter()
            formatter.dateStyle = .medium
            return formatter.string(from: date)
        }
    }
    
    private func deleteEntry(_ entry: FoodEntry) {
        withAnimation {
            modelContext.delete(entry)
        }
    }
}

struct HistoryEntryRow: View {
    let entry: FoodEntry
    
    var body: some View {
        HStack(spacing: 12) {
            Text(entry.emoji)
                .font(.title2)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(entry.name)
                    .font(.subheadline)
                    .fontWeight(.medium)
                
                HStack(spacing: 8) {
                    Text("\(entry.weightGrams)g")
                    Text("•")
                    Text(formatTime(entry.createdAt))
                }
                .font(.caption)
                .foregroundStyle(.secondary)
            }
            
            Spacer()
            
            VStack(alignment: .trailing, spacing: 2) {
                Text("\(entry.calories)")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundStyle(.orange)
                Text("kcal")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 4)
    }
    
    private func formatTime(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

#Preview {
    HistoryView()
        .modelContainer(for: [FoodEntry.self, FavoriteFood.self], inMemory: true)
}
