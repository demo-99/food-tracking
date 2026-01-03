// GeminiService - AI food analysis using Google Generative AI

import Foundation
import UIKit

@MainActor
class GeminiService: ObservableObject {
    private let apiKey: String
    private let baseURL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
    
    init(apiKey: String) {
        self.apiKey = apiKey
    }
    
    /// Analyze a food photo and estimate nutritional values
    func analyzePhoto(image: UIImage, hint: String? = nil) async throws -> FoodAnalysisResult {
        guard let imageData = image.jpegData(compressionQuality: 0.8) else {
            throw GeminiError.imageConversionFailed
        }
        
        let base64Image = imageData.base64EncodedString()
        
        let hintSection = hint.map { "\nAdditional context from user: \"\($0)\"\nUse this information to improve your estimate.\n" } ?? ""
        
        let prompt = """
        Analyze this food image and provide nutritional estimates.
        \(hintSection)
        Respond ONLY in this exact format with numbers (no ranges, pick the middle estimate):
        NAME: [food name]
        EMOJI: [single emoji that best represents this food]
        WEIGHT: [estimated portion weight in grams]
        CALORIES: [total calories for this portion]
        FATS: [grams of fat]
        PROTEINS: [grams of protein]
        CARBS: [grams of carbohydrates]
        
        Estimate the visible portion size. Be concise with the food name.
        Choose an emoji that visually represents the food (e.g., 🍕 for pizza, 🍌 for banana).
        """
        
        let requestBody: [String: Any] = [
            "contents": [
                [
                    "parts": [
                        ["text": prompt],
                        [
                            "inline_data": [
                                "mime_type": "image/jpeg",
                                "data": base64Image
                            ]
                        ]
                    ]
                ]
            ],
            "generationConfig": [
                "temperature": 0.4,
                "topK": 32,
                "topP": 1,
                "maxOutputTokens": 2048
            ]
        ]
        
        return try await sendRequest(body: requestBody)
    }
    
    /// Analyze a food description and estimate nutritional values
    func analyzeDescription(_ description: String) async throws -> FoodAnalysisResult {
        let prompt = """
        Estimate the nutritional values for this food: "\(description)"
        
        Respond ONLY in this exact format with numbers (no ranges, pick the middle estimate):
        NAME: [concise food name]
        EMOJI: [single emoji that best represents this food]
        WEIGHT: [typical portion weight in grams]
        CALORIES: [total calories for this portion]
        FATS: [grams of fat]
        PROTEINS: [grams of protein]
        CARBS: [grams of carbohydrates]
        
        Consider typical portion sizes. Be concise with the food name.
        Choose an emoji that visually represents the food.
        """
        
        let requestBody: [String: Any] = [
            "contents": [
                [
                    "parts": [
                        ["text": prompt]
                    ]
                ]
            ],
            "generationConfig": [
                "temperature": 0.4,
                "topK": 32,
                "topP": 1,
                "maxOutputTokens": 2048
            ]
        ]
        
        return try await sendRequest(body: requestBody)
    }
    
    private func sendRequest(body: [String: Any]) async throws -> FoodAnalysisResult {
        guard let url = URL(string: "\(baseURL)?key=\(apiKey)") else {
            throw GeminiError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: body)
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw GeminiError.requestFailed
        }
        
        guard let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
              let candidates = json["candidates"] as? [[String: Any]],
              let content = candidates.first?["content"] as? [String: Any],
              let parts = content["parts"] as? [[String: Any]],
              let text = parts.first?["text"] as? String else {
            throw GeminiError.parsingFailed
        }
        
        return parseResponse(text)
    }
    
    private func parseResponse(_ text: String) -> FoodAnalysisResult {
        var name = "Unknown Food"
        var emoji = "🍽️"
        var calories = 0
        var fats: Float = 0
        var proteins: Float = 0
        var carbs: Float = 0
        var weight = 100
        
        for line in text.components(separatedBy: .newlines) {
            let trimmed = line.trimmingCharacters(in: .whitespaces)
            
            if trimmed.lowercased().hasPrefix("name:") {
                name = String(trimmed.dropFirst(5)).trimmingCharacters(in: .whitespaces)
            } else if trimmed.lowercased().hasPrefix("emoji:") {
                let emojiText = String(trimmed.dropFirst(6)).trimmingCharacters(in: .whitespaces)
                emoji = extractEmoji(from: emojiText) ?? "🍽️"
            } else if trimmed.lowercased().hasPrefix("weight:") {
                weight = max(1, extractNumber(from: String(trimmed.dropFirst(7))))
            } else if trimmed.lowercased().hasPrefix("calories:") {
                calories = extractNumber(from: String(trimmed.dropFirst(9)))
            } else if trimmed.lowercased().hasPrefix("fats:") {
                fats = extractFloat(from: String(trimmed.dropFirst(5)))
            } else if trimmed.lowercased().hasPrefix("proteins:") {
                proteins = extractFloat(from: String(trimmed.dropFirst(9)))
            } else if trimmed.lowercased().hasPrefix("carbs:") {
                carbs = extractFloat(from: String(trimmed.dropFirst(6)))
            }
        }
        
        return FoodAnalysisResult(
            name: name,
            calories: calories,
            fats: fats,
            proteins: proteins,
            carbs: carbs,
            emoji: emoji,
            weightGrams: weight
        )
    }
    
    private func extractNumber(from text: String) -> Int {
        let cleaned = text.trimmingCharacters(in: .whitespaces)
            .components(separatedBy: CharacterSet.decimalDigits.inverted)
            .joined()
        return Int(cleaned) ?? 0
    }
    
    private func extractFloat(from text: String) -> Float {
        let cleaned = text.trimmingCharacters(in: .whitespaces)
        let numberChars = CharacterSet(charactersIn: "0123456789.")
        let filtered = String(cleaned.unicodeScalars.filter { numberChars.contains($0) })
        return Float(filtered) ?? 0
    }
    
    private func extractEmoji(from text: String) -> String? {
        for scalar in text.unicodeScalars {
            if scalar.properties.isEmoji && scalar.properties.isEmojiPresentation {
                return String(scalar)
            }
        }
        // Check for emoji sequences
        for char in text {
            if char.unicodeScalars.first?.properties.isEmoji == true {
                return String(char)
            }
        }
        return nil
    }
}

enum GeminiError: LocalizedError {
    case imageConversionFailed
    case invalidURL
    case requestFailed
    case parsingFailed
    
    var errorDescription: String? {
        switch self {
        case .imageConversionFailed: return "Failed to convert image"
        case .invalidURL: return "Invalid API URL"
        case .requestFailed: return "API request failed"
        case .parsingFailed: return "Failed to parse response"
        }
    }
}
