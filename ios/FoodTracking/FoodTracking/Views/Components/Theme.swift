// Color Theme - Matching Android theme colors

import SwiftUI

extension Color {
    // Brand colors matching Android theme
    static let caloriesColor = Color.orange
    static let proteinsColor = Color.blue
    static let carbsColor = Color.green
    static let fatsColor = Color.purple
    
    // Custom colors
    static let primaryContainer = Color.orange.opacity(0.15)
    static let secondaryContainer = Color.purple.opacity(0.15)
    static let surfaceContainer = Color(.systemGray6)
}

// Theme configuration
struct AppTheme {
    static let cornerRadius: CGFloat = 12
    static let cardCornerRadius: CGFloat = 16
    static let spacing: CGFloat = 16
    static let smallSpacing: CGFloat = 8
}
