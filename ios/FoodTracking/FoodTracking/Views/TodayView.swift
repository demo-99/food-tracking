// TodayView - Main screen showing today's food entries

import SwiftUI
import SwiftData

struct TodayView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(filter: #Predicate<FoodEntry> { entry in
        Calendar.current.isDateInToday(entry.date)
    }, sort: \FoodEntry.createdAt, order: .descending) private var todayEntries: [FoodEntry]
    
    @StateObject private var settingsManager = SettingsManager()
    @State private var showAddFood = false
    
    private var totalCalories: Int {
        todayEntries.reduce(0) { $0 + $1.calories }
    }
    
    private var totalProtein: Float {
        todayEntries.reduce(0) { $0 + $1.proteins }
    }
    
    private var totalCarbs: Float {
        todayEntries.reduce(0) { $0 + $1.carbs }
    }
    
    private var totalFat: Float {
        todayEntries.reduce(0) { $0 + $1.fats }
    }
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Summary Card
                    summaryCard
                    
                    // Macro breakdown
                    macroBreakdown
                    
                    // Food entries
                    if todayEntries.isEmpty {
                        emptyState
                    } else {
                        foodEntriesList
                    }
                }
                .padding()
            }
            .navigationTitle("Today")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        showAddFood = true
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.title2)
                    }
                }
            }
            .sheet(isPresented: $showAddFood) {
                AddFoodView()
            }
        }
    }
    
    private var summaryCard: some View {
        VStack(spacing: 12) {
            HStack {
                VStack(alignment: .leading) {
                    Text("\(totalCalories)")
                        .font(.system(size: 48, weight: .bold))
                        .foregroundStyle(.orange)
                    Text("of \(settingsManager.calorieGoal) kcal")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                
                Spacer()
                
                // Progress ring
                ZStack {
                    Circle()
                        .stroke(Color.orange.opacity(0.2), lineWidth: 12)
                        .frame(width: 80, height: 80)
                    
                    Circle()
                        .trim(from: 0, to: min(CGFloat(totalCalories) / CGFloat(settingsManager.calorieGoal), 1.0))
                        .stroke(Color.orange, style: StrokeStyle(lineWidth: 12, lineCap: .round))
                        .frame(width: 80, height: 80)
                        .rotationEffect(.degrees(-90))
                    
                    Text("\(Int(min(Double(totalCalories) / Double(settingsManager.calorieGoal) * 100, 100)))%")
                        .font(.caption)
                        .fontWeight(.bold)
                }
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.1), radius: 8, x: 0, y: 4)
    }
    
    private var macroBreakdown: some View {
        HStack(spacing: 16) {
            MacroCard(
                name: "Protein",
                current: Int(totalProtein),
                goal: settingsManager.proteinGoal,
                color: .blue,
                unit: "g"
            )
            
            MacroCard(
                name: "Carbs",
                current: Int(totalCarbs),
                goal: settingsManager.carbsGoal,
                color: .green,
                unit: "g"
            )
            
            MacroCard(
                name: "Fat",
                current: Int(totalFat),
                goal: settingsManager.fatsGoal,
                color: .purple,
                unit: "g"
            )
        }
    }
    
    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "fork.knife.circle")
                .font(.system(size: 60))
                .foregroundStyle(.secondary)
            
            Text("No food logged today")
                .font(.title3)
                .fontWeight(.medium)
            
            Text("Tap + to add your first meal")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            
            Button {
                showAddFood = true
            } label: {
                Label("Add Food", systemImage: "plus")
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
            }
            .buttonStyle(.borderedProminent)
        }
        .padding(.vertical, 40)
    }
    
    private var foodEntriesList: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Today's Food")
                .font(.headline)
            
            ForEach(todayEntries) { entry in
                FoodEntryCard(entry: entry) {
                    deleteEntry(entry)
                }
            }
        }
    }
    
    private func deleteEntry(_ entry: FoodEntry) {
        withAnimation {
            modelContext.delete(entry)
        }
    }
}

struct MacroCard: View {
    let name: String
    let current: Int
    let goal: Int
    let color: Color
    let unit: String
    
    private var progress: Double {
        guard goal > 0 else { return 0 }
        return min(Double(current) / Double(goal), 1.0)
    }
    
    var body: some View {
        VStack(spacing: 8) {
            Text(name)
                .font(.caption)
                .foregroundStyle(.secondary)
            
            ZStack {
                Circle()
                    .stroke(color.opacity(0.2), lineWidth: 6)
                    .frame(width: 50, height: 50)
                
                Circle()
                    .trim(from: 0, to: progress)
                    .stroke(color, style: StrokeStyle(lineWidth: 6, lineCap: .round))
                    .frame(width: 50, height: 50)
                    .rotationEffect(.degrees(-90))
            }
            
            Text("\(current)\(unit)")
                .font(.subheadline)
                .fontWeight(.semibold)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.05), radius: 4, x: 0, y: 2)
    }
}

struct FoodEntryCard: View {
    let entry: FoodEntry
    let onDelete: () -> Void
    
    var body: some View {
        HStack(spacing: 12) {
            Text(entry.emoji)
                .font(.title)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(entry.name)
                    .font(.headline)
                
                Text("\(entry.weightGrams)g")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            
            Spacer()
            
            VStack(alignment: .trailing, spacing: 4) {
                Text("\(entry.calories) kcal")
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundStyle(.orange)
                
                Text("P:\(Int(entry.proteins))g C:\(Int(entry.carbs))g F:\(Int(entry.fats))g")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.05), radius: 4, x: 0, y: 2)
        .swipeActions(edge: .trailing) {
            Button(role: .destructive) {
                onDelete()
            } label: {
                Label("Delete", systemImage: "trash")
            }
        }
    }
}

#Preview {
    TodayView()
        .modelContainer(for: [FoodEntry.self, FavoriteFood.self], inMemory: true)
}
