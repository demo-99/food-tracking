// SettingsManager - User preferences storage

import Foundation
import SwiftUI

@MainActor
class SettingsManager: ObservableObject {
    private let defaults = UserDefaults.standard
    
    // MARK: - Keys
    private enum Keys {
        static let geminiApiKey = "geminiApiKey"
        static let calorieGoal = "calorieGoal"
        static let proteinGoal = "proteinGoal"
        static let carbsGoal = "carbsGoal"
        static let fatsGoal = "fatsGoal"
        static let healthKitEnabled = "healthKitEnabled"
        static let onboardingComplete = "onboardingComplete"
        static let selectedProfile = "selectedProfile"
    }
    
    // MARK: - Published Properties
    @Published var geminiApiKey: String {
        didSet { defaults.set(geminiApiKey, forKey: Keys.geminiApiKey) }
    }
    
    @Published var calorieGoal: Int {
        didSet { defaults.set(calorieGoal, forKey: Keys.calorieGoal) }
    }
    
    @Published var proteinGoal: Int {
        didSet { defaults.set(proteinGoal, forKey: Keys.proteinGoal) }
    }
    
    @Published var carbsGoal: Int {
        didSet { defaults.set(carbsGoal, forKey: Keys.carbsGoal) }
    }
    
    @Published var fatsGoal: Int {
        didSet { defaults.set(fatsGoal, forKey: Keys.fatsGoal) }
    }
    
    @Published var healthKitEnabled: Bool {
        didSet { defaults.set(healthKitEnabled, forKey: Keys.healthKitEnabled) }
    }
    
    @Published var selectedProfile: NutritionProfile {
        didSet { defaults.set(selectedProfile.rawValue, forKey: Keys.selectedProfile) }
    }
    
    var hasGeminiApiKey: Bool {
        !geminiApiKey.isEmpty
    }
    
    // MARK: - Initialization
    init() {
        self.geminiApiKey = defaults.string(forKey: Keys.geminiApiKey) ?? ""
        self.calorieGoal = defaults.integer(forKey: Keys.calorieGoal) == 0 ? 2000 : defaults.integer(forKey: Keys.calorieGoal)
        self.proteinGoal = defaults.integer(forKey: Keys.proteinGoal) == 0 ? 50 : defaults.integer(forKey: Keys.proteinGoal)
        self.carbsGoal = defaults.integer(forKey: Keys.carbsGoal) == 0 ? 250 : defaults.integer(forKey: Keys.carbsGoal)
        self.fatsGoal = defaults.integer(forKey: Keys.fatsGoal) == 0 ? 65 : defaults.integer(forKey: Keys.fatsGoal)
        self.healthKitEnabled = defaults.bool(forKey: Keys.healthKitEnabled)
        
        let profileRaw = defaults.string(forKey: Keys.selectedProfile) ?? NutritionProfile.maintenance.rawValue
        self.selectedProfile = NutritionProfile(rawValue: profileRaw) ?? .maintenance
    }
    
    // MARK: - Onboarding
    func isOnboardingComplete() -> Bool {
        defaults.bool(forKey: Keys.onboardingComplete)
    }
    
    func completeOnboarding() {
        defaults.set(true, forKey: Keys.onboardingComplete)
    }
    
    // MARK: - Profile Application
    func applyProfile(_ profile: NutritionProfile, weight: Int = 70, height: Int = 170, age: Int = 30, isMale: Bool = true) {
        selectedProfile = profile
        
        // Calculate BMR using Mifflin-St Jeor equation
        let bmr: Double
        if isMale {
            bmr = (10.0 * Double(weight)) + (6.25 * Double(height)) - (5.0 * Double(age)) + 5
        } else {
            bmr = (10.0 * Double(weight)) + (6.25 * Double(height)) - (5.0 * Double(age)) - 161
        }
        
        // TDEE with moderate activity (1.55)
        let tdee = bmr * 1.55
        
        switch profile {
        case .weightLoss:
            calorieGoal = Int(tdee * 0.8) // 20% deficit
            proteinGoal = Int(Double(weight) * 2.0) // 2g per kg
            fatsGoal = Int(Double(calorieGoal) * 0.25 / 9) // 25% from fat
            carbsGoal = (calorieGoal - (proteinGoal * 4) - (fatsGoal * 9)) / 4
            
        case .maintenance:
            calorieGoal = Int(tdee)
            proteinGoal = Int(Double(weight) * 1.6) // 1.6g per kg
            fatsGoal = Int(Double(calorieGoal) * 0.30 / 9) // 30% from fat
            carbsGoal = (calorieGoal - (proteinGoal * 4) - (fatsGoal * 9)) / 4
            
        case .muscleGain:
            calorieGoal = Int(tdee * 1.15) // 15% surplus
            proteinGoal = Int(Double(weight) * 2.2) // 2.2g per kg
            fatsGoal = Int(Double(calorieGoal) * 0.25 / 9) // 25% from fat
            carbsGoal = (calorieGoal - (proteinGoal * 4) - (fatsGoal * 9)) / 4
            
        case .custom:
            // Keep current values for custom
            break
        }
    }
}

enum NutritionProfile: String, CaseIterable, Identifiable {
    case weightLoss = "Weight Loss"
    case maintenance = "Maintenance"
    case muscleGain = "Muscle Gain"
    case custom = "Custom"
    
    var id: String { rawValue }
    
    var description: String {
        switch self {
        case .weightLoss: return "Calorie deficit for fat loss"
        case .maintenance: return "Maintain current weight"
        case .muscleGain: return "Calorie surplus for building muscle"
        case .custom: return "Set your own goals"
        }
    }
    
    var icon: String {
        switch self {
        case .weightLoss: return "arrow.down.circle"
        case .maintenance: return "equal.circle"
        case .muscleGain: return "arrow.up.circle"
        case .custom: return "slider.horizontal.3"
        }
    }
}
