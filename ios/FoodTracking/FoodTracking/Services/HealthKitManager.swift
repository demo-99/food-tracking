// HealthKitManager - HealthKit integration (iOS equivalent of Health Connect)

import Foundation
import HealthKit

@MainActor
class HealthKitManager: ObservableObject {
    private let healthStore = HKHealthStore()
    
    @Published var isAuthorized = false
    @Published var isAvailable = HKHealthStore.isHealthDataAvailable()
    
    // Nutrition types we want to write
    private let nutritionTypes: Set<HKSampleType> = {
        var types = Set<HKSampleType>()
        if let calories = HKQuantityType.quantityType(forIdentifier: .dietaryEnergyConsumed) {
            types.insert(calories)
        }
        if let protein = HKQuantityType.quantityType(forIdentifier: .dietaryProtein) {
            types.insert(protein)
        }
        if let carbs = HKQuantityType.quantityType(forIdentifier: .dietaryCarbohydrates) {
            types.insert(carbs)
        }
        if let fat = HKQuantityType.quantityType(forIdentifier: .dietaryFatTotal) {
            types.insert(fat)
        }
        return types
    }()
    
    /// Request HealthKit authorization
    func requestAuthorization() async throws {
        guard isAvailable else {
            throw HealthKitError.notAvailable
        }
        
        try await healthStore.requestAuthorization(toShare: nutritionTypes, read: nutritionTypes)
        isAuthorized = true
    }
    
    /// Check if we have authorization
    func checkAuthorization() -> Bool {
        guard isAvailable else { return false }
        
        for type in nutritionTypes {
            if healthStore.authorizationStatus(for: type) != .sharingAuthorized {
                return false
            }
        }
        return true
    }
    
    /// Sync a food entry to HealthKit
    func syncFoodEntry(_ entry: FoodEntry) async throws -> String? {
        guard isAvailable && isAuthorized else { return nil }
        
        var samples: [HKQuantitySample] = []
        let now = entry.date
        let metadata: [String: Any] = [
            HKMetadataKeyFoodType: entry.name,
            "FoodTrackingAppEntryId": entry.id.uuidString
        ]
        
        // Calories
        if let calorieType = HKQuantityType.quantityType(forIdentifier: .dietaryEnergyConsumed) {
            let quantity = HKQuantity(unit: .kilocalorie(), doubleValue: Double(entry.calories))
            let sample = HKQuantitySample(type: calorieType, quantity: quantity, start: now, end: now, metadata: metadata)
            samples.append(sample)
        }
        
        // Protein
        if entry.proteins > 0, let proteinType = HKQuantityType.quantityType(forIdentifier: .dietaryProtein) {
            let quantity = HKQuantity(unit: .gram(), doubleValue: Double(entry.proteins))
            let sample = HKQuantitySample(type: proteinType, quantity: quantity, start: now, end: now, metadata: metadata)
            samples.append(sample)
        }
        
        // Carbs
        if entry.carbs > 0, let carbType = HKQuantityType.quantityType(forIdentifier: .dietaryCarbohydrates) {
            let quantity = HKQuantity(unit: .gram(), doubleValue: Double(entry.carbs))
            let sample = HKQuantitySample(type: carbType, quantity: quantity, start: now, end: now, metadata: metadata)
            samples.append(sample)
        }
        
        // Fat
        if entry.fats > 0, let fatType = HKQuantityType.quantityType(forIdentifier: .dietaryFatTotal) {
            let quantity = HKQuantity(unit: .gram(), doubleValue: Double(entry.fats))
            let sample = HKQuantitySample(type: fatType, quantity: quantity, start: now, end: now, metadata: metadata)
            samples.append(sample)
        }
        
        guard !samples.isEmpty else { return nil }
        
        try await healthStore.save(samples)
        
        // Return the first sample's UUID as the record ID
        return samples.first?.uuid.uuidString
    }
    
    /// Delete a food entry from HealthKit
    func deleteFoodEntry(recordId: String) async throws {
        guard isAvailable && isAuthorized else { return }
        
        // We need to find and delete samples with matching metadata
        // This is a simplified version - in production you'd want to track sample UUIDs
        guard let uuid = UUID(uuidString: recordId) else { return }
        
        let predicate = HKQuery.predicateForObject(with: uuid)
        
        for type in nutritionTypes {
            let samples = try await withCheckedThrowingContinuation { continuation in
                let query = HKSampleQuery(sampleType: type, predicate: predicate, limit: 10, sortDescriptors: nil) { _, samples, error in
                    if let error = error {
                        continuation.resume(throwing: error)
                    } else {
                        continuation.resume(returning: samples ?? [])
                    }
                }
                healthStore.execute(query)
            }
            
            for sample in samples {
                try await healthStore.delete(sample)
            }
        }
    }
    
    /// Get today's totals from HealthKit
    func getTodayTotals() async throws -> (calories: Int, protein: Float, carbs: Float, fat: Float) {
        guard isAvailable && isAuthorized else { return (0, 0, 0, 0) }
        
        let calendar = Calendar.current
        let now = Date()
        let startOfDay = calendar.startOfDay(for: now)
        let predicate = HKQuery.predicateForSamples(withStart: startOfDay, end: now, options: .strictStartDate)
        
        async let caloriesTask = getSum(for: .dietaryEnergyConsumed, unit: .kilocalorie(), predicate: predicate)
        async let proteinTask = getSum(for: .dietaryProtein, unit: .gram(), predicate: predicate)
        async let carbsTask = getSum(for: .dietaryCarbohydrates, unit: .gram(), predicate: predicate)
        async let fatTask = getSum(for: .dietaryFatTotal, unit: .gram(), predicate: predicate)
        
        let (calories, protein, carbs, fat) = await (caloriesTask, proteinTask, carbsTask, fatTask)
        
        return (Int(calories), Float(protein), Float(carbs), Float(fat))
    }
    
    private func getSum(for identifier: HKQuantityTypeIdentifier, unit: HKUnit, predicate: NSPredicate) async -> Double {
        guard let quantityType = HKQuantityType.quantityType(forIdentifier: identifier) else { return 0 }
        
        return await withCheckedContinuation { continuation in
            let query = HKStatisticsQuery(quantityType: quantityType, quantitySamplePredicate: predicate, options: .cumulativeSum) { _, statistics, _ in
                let sum = statistics?.sumQuantity()?.doubleValue(for: unit) ?? 0
                continuation.resume(returning: sum)
            }
            healthStore.execute(query)
        }
    }
}

enum HealthKitError: LocalizedError {
    case notAvailable
    case authorizationDenied
    
    var errorDescription: String? {
        switch self {
        case .notAvailable: return "HealthKit is not available on this device"
        case .authorizationDenied: return "HealthKit authorization denied"
        }
    }
}
