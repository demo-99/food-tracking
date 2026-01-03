// AddFoodView - Screen for adding new food entries

import SwiftUI
import SwiftData
import PhotosUI

struct AddFoodView: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    
    @State private var selectedTab = 0
    @StateObject private var settingsManager = SettingsManager()
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Tab selector
                Picker("Input Method", selection: $selectedTab) {
                    Text("📷 Photo").tag(0)
                    Text("📝 Describe").tag(1)
                    Text("♥️ Favorites").tag(2)
                    Text("🔍 Search").tag(3)
                    Text("✏️ Manual").tag(4)
                }
                .pickerStyle(.segmented)
                .padding()
                
                Divider()
                
                // Tab content
                Group {
                    switch selectedTab {
                    case 0:
                        PhotoTab(settingsManager: settingsManager) { entry in
                            saveEntry(entry)
                        }
                    case 1:
                        DescriptionTab(settingsManager: settingsManager) { entry in
                            saveEntry(entry)
                        }
                    case 2:
                        FavoritesTab { entry in
                            saveEntry(entry)
                        }
                    case 3:
                        SearchTab { entry in
                            saveEntry(entry)
                        }
                    case 4:
                        ManualTab { entry in
                            saveEntry(entry)
                        }
                    default:
                        EmptyView()
                    }
                }
            }
            .navigationTitle("Add Food")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
            }
        }
    }
    
    private func saveEntry(_ entry: FoodEntry) {
        modelContext.insert(entry)
        dismiss()
    }
}

// MARK: - Photo Tab
struct PhotoTab: View {
    let settingsManager: SettingsManager
    let onSave: (FoodEntry) -> Void
    
    @State private var selectedImage: UIImage?
    @State private var photoPickerItem: PhotosPickerItem?
    @State private var showCamera = false
    @State private var hint = ""
    @State private var isAnalyzing = false
    @State private var analysisResult: FoodAnalysisResult?
    @State private var adjustedWeight: Int?
    @State private var errorMessage: String?
    
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                if let image = selectedImage {
                    // Image preview
                    Image(uiImage: image)
                        .resizable()
                        .scaledToFill()
                        .frame(height: 200)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                    
                    if analysisResult == nil {
                        // Hint field
                        TextField("Additional info (optional)", text: $hint)
                            .textFieldStyle(.roundedBorder)
                        
                        HStack {
                            Button("Change") {
                                selectedImage = nil
                                analysisResult = nil
                            }
                            .buttonStyle(.bordered)
                            
                            Button {
                                analyzePhoto()
                            } label: {
                                if isAnalyzing {
                                    ProgressView()
                                        .progressViewStyle(.circular)
                                } else {
                                    Label("Analyze", systemImage: "sparkles")
                                }
                            }
                            .buttonStyle(.borderedProminent)
                            .disabled(isAnalyzing || !settingsManager.hasGeminiApiKey)
                        }
                    }
                } else {
                    // Photo/Gallery selection
                    HStack(spacing: 16) {
                        Button {
                            showCamera = true
                        } label: {
                            VStack(spacing: 12) {
                                Image(systemName: "camera.fill")
                                    .font(.largeTitle)
                                Text("Take Photo")
                                    .font(.subheadline)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 140)
                            .background(Color.blue.opacity(0.1))
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                        .foregroundStyle(.blue)
                        
                        PhotosPicker(selection: $photoPickerItem, matching: .images) {
                            VStack(spacing: 12) {
                                Image(systemName: "photo.on.rectangle")
                                    .font(.largeTitle)
                                Text("Gallery")
                                    .font(.subheadline)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 140)
                            .background(Color.purple.opacity(0.1))
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                        .foregroundStyle(.purple)
                    }
                }
                
                // Error message
                if let error = errorMessage {
                    Text(error)
                        .foregroundStyle(.red)
                        .font(.caption)
                }
                
                // Analysis result
                if let result = analysisResult {
                    AnalysisResultCard(
                        result: result,
                        adjustedWeight: $adjustedWeight,
                        onSave: {
                            let finalResult = adjustedWeight != nil ? result.scaledTo(newWeightGrams: adjustedWeight!) : result
                            onSave(finalResult.toFoodEntry(source: .photo))
                        }
                    )
                }
            }
            .padding()
        }
        .onChange(of: photoPickerItem) { _, newItem in
            Task {
                if let data = try? await newItem?.loadTransferable(type: Data.self),
                   let image = UIImage(data: data) {
                    selectedImage = image
                }
            }
        }
        .sheet(isPresented: $showCamera) {
            CameraView(image: $selectedImage)
        }
    }
    
    private func analyzePhoto() {
        guard let image = selectedImage else { return }
        guard settingsManager.hasGeminiApiKey else {
            errorMessage = "Please set your Gemini API key in Settings"
            return
        }
        
        isAnalyzing = true
        errorMessage = nil
        
        Task {
            do {
                let service = GeminiService(apiKey: settingsManager.geminiApiKey)
                let result = try await service.analyzePhoto(image: image, hint: hint.isEmpty ? nil : hint)
                analysisResult = result
                adjustedWeight = result.weightGrams
            } catch {
                errorMessage = error.localizedDescription
            }
            isAnalyzing = false
        }
    }
}

// MARK: - Description Tab
struct DescriptionTab: View {
    let settingsManager: SettingsManager
    let onSave: (FoodEntry) -> Void
    
    @State private var description = ""
    @State private var isAnalyzing = false
    @State private var analysisResult: FoodAnalysisResult?
    @State private var adjustedWeight: Int?
    @State private var errorMessage: String?
    
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                TextField("Describe what you ate...", text: $description, axis: .vertical)
                    .textFieldStyle(.roundedBorder)
                    .lineLimit(3...5)
                
                Button {
                    analyzeDescription()
                } label: {
                    if isAnalyzing {
                        ProgressView()
                            .progressViewStyle(.circular)
                    } else {
                        Text("Analyze with AI")
                    }
                }
                .buttonStyle(.borderedProminent)
                .disabled(description.isEmpty || isAnalyzing || !settingsManager.hasGeminiApiKey)
                
                if let error = errorMessage {
                    Text(error)
                        .foregroundStyle(.red)
                        .font(.caption)
                }
                
                if let result = analysisResult {
                    AnalysisResultCard(
                        result: result,
                        adjustedWeight: $adjustedWeight,
                        onSave: {
                            let finalResult = adjustedWeight != nil ? result.scaledTo(newWeightGrams: adjustedWeight!) : result
                            onSave(finalResult.toFoodEntry(source: .description))
                        }
                    )
                }
            }
            .padding()
        }
    }
    
    private func analyzeDescription() {
        guard !description.isEmpty else { return }
        guard settingsManager.hasGeminiApiKey else {
            errorMessage = "Please set your Gemini API key in Settings"
            return
        }
        
        isAnalyzing = true
        errorMessage = nil
        
        Task {
            do {
                let service = GeminiService(apiKey: settingsManager.geminiApiKey)
                let result = try await service.analyzeDescription(description)
                analysisResult = result
                adjustedWeight = result.weightGrams
            } catch {
                errorMessage = error.localizedDescription
            }
            isAnalyzing = false
        }
    }
}

// MARK: - Favorites Tab
struct FavoritesTab: View {
    @Query(sort: \FavoriteFood.name) private var favorites: [FavoriteFood]
    let onSave: (FoodEntry) -> Void
    
    var body: some View {
        if favorites.isEmpty {
            VStack(spacing: 16) {
                Text("💔")
                    .font(.system(size: 60))
                Text("No favorites yet")
                    .font(.title3)
                    .fontWeight(.medium)
                Text("Swipe right on foods in the main screen to favorite them")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding()
        } else {
            List(favorites) { favorite in
                Button {
                    onSave(favorite.toFoodEntry())
                } label: {
                    HStack {
                        Text(favorite.emoji)
                            .font(.title2)
                        
                        VStack(alignment: .leading) {
                            Text(favorite.name)
                                .font(.headline)
                        }
                        
                        Spacer()
                        
                        VStack(alignment: .trailing) {
                            Text("\(favorite.calories) kcal")
                                .font(.subheadline)
                                .foregroundStyle(.orange)
                            Text("P:\(Int(favorite.proteins))g C:\(Int(favorite.carbs))g F:\(Int(favorite.fats))g")
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .foregroundStyle(.primary)
            }
        }
    }
}

// MARK: - Search Tab
struct SearchTab: View {
    let onSave: (FoodEntry) -> Void
    
    @State private var searchQuery = ""
    
    private var searchResults: [CommonFood] {
        CommonFoodsDatabase.search(query: searchQuery)
    }
    
    var body: some View {
        VStack(spacing: 0) {
            TextField("Search food...", text: $searchQuery)
                .textFieldStyle(.roundedBorder)
                .padding()
            
            List(searchResults) { food in
                Button {
                    onSave(food.toFoodEntry())
                } label: {
                    HStack {
                        Text(food.emoji)
                            .font(.title2)
                        
                        VStack(alignment: .leading) {
                            Text(food.name)
                                .font(.headline)
                            Text(food.servingSize)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        
                        Spacer()
                        
                        VStack(alignment: .trailing) {
                            Text("\(food.calories) kcal")
                                .font(.subheadline)
                                .foregroundStyle(.orange)
                            Text("P:\(Int(food.proteins))g C:\(Int(food.carbs))g F:\(Int(food.fats))g")
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .foregroundStyle(.primary)
            }
        }
    }
}

// MARK: - Manual Tab
struct ManualTab: View {
    let onSave: (FoodEntry) -> Void
    
    @State private var name = ""
    @State private var calories = ""
    @State private var proteins = ""
    @State private var carbs = ""
    @State private var fats = ""
    @State private var weight = "100"
    
    var body: some View {
        Form {
            Section("Food Details") {
                TextField("Food name", text: $name)
                TextField("Weight (g)", text: $weight)
                    .keyboardType(.numberPad)
            }
            
            Section("Nutrition") {
                TextField("Calories (kcal)", text: $calories)
                    .keyboardType(.numberPad)
                TextField("Protein (g)", text: $proteins)
                    .keyboardType(.decimalPad)
                TextField("Carbs (g)", text: $carbs)
                    .keyboardType(.decimalPad)
                TextField("Fat (g)", text: $fats)
                    .keyboardType(.decimalPad)
            }
            
            Section {
                Button("Add Food") {
                    saveManualEntry()
                }
                .disabled(name.isEmpty)
            }
        }
    }
    
    private func saveManualEntry() {
        let entry = FoodEntry(
            name: name,
            calories: Int(calories) ?? 0,
            fats: Float(fats) ?? 0,
            proteins: Float(proteins) ?? 0,
            carbs: Float(carbs) ?? 0,
            weightGrams: Int(weight) ?? 100,
            source: .manual
        )
        onSave(entry)
    }
}

// MARK: - Analysis Result Card
struct AnalysisResultCard: View {
    let result: FoodAnalysisResult
    @Binding var adjustedWeight: Int?
    let onSave: () -> Void
    
    private var effectiveResult: FoodAnalysisResult {
        guard let weight = adjustedWeight, weight != result.weightGrams else {
            return result
        }
        return result.scaledTo(newWeightGrams: weight)
    }
    
    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Text(effectiveResult.emoji)
                    .font(.largeTitle)
                
                VStack(alignment: .leading) {
                    Text("✨ AI Analysis")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Text(effectiveResult.name)
                        .font(.title2)
                        .fontWeight(.bold)
                }
                
                Spacer()
            }
            
            // Weight slider
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("Portion Size")
                        .font(.subheadline)
                        .fontWeight(.medium)
                    
                    Spacer()
                    
                    TextField("g", value: $adjustedWeight, format: .number)
                        .textFieldStyle(.roundedBorder)
                        .frame(width: 80)
                        .keyboardType(.numberPad)
                }
                
                Slider(
                    value: Binding(
                        get: { Double(adjustedWeight ?? result.weightGrams) },
                        set: { adjustedWeight = Int($0) }
                    ),
                    in: 0...Double(result.weightGrams * 3),
                    step: 1
                )
            }
            
            Divider()
            
            // Nutrition display
            HStack {
                NutrientDisplay(label: "Calories", value: "\(effectiveResult.calories)", unit: "kcal", color: .orange)
                NutrientDisplay(label: "Protein", value: "\(Int(effectiveResult.proteins))", unit: "g", color: .blue)
                NutrientDisplay(label: "Carbs", value: "\(Int(effectiveResult.carbs))", unit: "g", color: .green)
                NutrientDisplay(label: "Fat", value: "\(Int(effectiveResult.fats))", unit: "g", color: .purple)
            }
            
            Button {
                onSave()
            } label: {
                Label("Save to Today", systemImage: "checkmark")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
        .background(Color.orange.opacity(0.1))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

struct NutrientDisplay: View {
    let label: String
    let value: String
    let unit: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 4) {
            Text(label)
                .font(.caption)
                .foregroundStyle(.secondary)
            Text(value)
                .font(.headline)
                .foregroundStyle(color)
            Text(unit)
                .font(.caption2)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
}

// MARK: - Camera View
struct CameraView: UIViewControllerRepresentable {
    @Binding var image: UIImage?
    @Environment(\.dismiss) private var dismiss
    
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.delegate = context.coordinator
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: CameraView
        
        init(_ parent: CameraView) {
            self.parent = parent
        }
        
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let image = info[.originalImage] as? UIImage {
                parent.image = image
            }
            parent.dismiss()
        }
        
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            parent.dismiss()
        }
    }
}

#Preview {
    AddFoodView()
        .modelContainer(for: [FoodEntry.self, FavoriteFood.self], inMemory: true)
}
