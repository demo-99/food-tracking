// OnboardingView - First-time user setup

import SwiftUI

struct OnboardingView: View {
    @Binding var isComplete: Bool
    @StateObject private var settingsManager = SettingsManager()
    @StateObject private var healthKitManager = HealthKitManager()
    
    @State private var currentPage = 0
    @State private var apiKey = ""
    @State private var selectedProfile: NutritionProfile = .maintenance
    @State private var weight = 70
    @State private var height = 170
    @State private var age = 30
    @State private var isMale = true
    @State private var enableHealthKit = true
    
    var body: some View {
        TabView(selection: $currentPage) {
            // Welcome Page
            welcomePage
                .tag(0)
            
            // Profile Setup Page
            profilePage
                .tag(1)
            
            // Goals Page
            goalsPage
                .tag(2)
            
            // API Key Page
            apiKeyPage
                .tag(3)
            
            // Health Integration Page
            healthPage
                .tag(4)
        }
        .tabViewStyle(.page(indexDisplayMode: .always))
        .indexViewStyle(.page(backgroundDisplayMode: .always))
    }
    
    private var welcomePage: some View {
        VStack(spacing: 24) {
            Spacer()
            
            Image(systemName: "fork.knife.circle.fill")
                .font(.system(size: 100))
                .foregroundStyle(.orange)
            
            Text("Food Tracking")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Text("Track your nutrition effortlessly with AI-powered food analysis")
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
            
            Spacer()
            
            Button {
                withAnimation {
                    currentPage = 1
                }
            } label: {
                Text("Get Started")
                    .font(.headline)
                    .frame(maxWidth: .infinity)
                    .padding()
            }
            .buttonStyle(.borderedProminent)
            .padding(.horizontal, 40)
            .padding(.bottom, 40)
        }
    }
    
    private var profilePage: some View {
        VStack(spacing: 24) {
            Text("About You")
                .font(.title)
                .fontWeight(.bold)
            
            Text("Help us calculate your nutrition goals")
                .foregroundStyle(.secondary)
            
            Form {
                Picker("Gender", selection: $isMale) {
                    Text("Male").tag(true)
                    Text("Female").tag(false)
                }
                
                HStack {
                    Text("Age")
                    Spacer()
                    TextField("Years", value: $age, format: .number)
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.trailing)
                        .frame(width: 80)
                }
                
                HStack {
                    Text("Weight")
                    Spacer()
                    TextField("kg", value: $weight, format: .number)
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.trailing)
                        .frame(width: 80)
                }
                
                HStack {
                    Text("Height")
                    Spacer()
                    TextField("cm", value: $height, format: .number)
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.trailing)
                        .frame(width: 80)
                }
            }
            .scrollContentBackground(.hidden)
            
            navigationButtons
        }
        .padding()
    }
    
    private var goalsPage: some View {
        VStack(spacing: 24) {
            Text("Your Goal")
                .font(.title)
                .fontWeight(.bold)
            
            Text("What would you like to achieve?")
                .foregroundStyle(.secondary)
            
            VStack(spacing: 12) {
                ForEach([NutritionProfile.weightLoss, .maintenance, .muscleGain], id: \.self) { profile in
                    Button {
                        selectedProfile = profile
                    } label: {
                        HStack {
                            Image(systemName: profile.icon)
                                .font(.title2)
                                .frame(width: 40)
                            
                            VStack(alignment: .leading) {
                                Text(profile.rawValue)
                                    .font(.headline)
                                Text(profile.description)
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                            
                            Spacer()
                            
                            if selectedProfile == profile {
                                Image(systemName: "checkmark.circle.fill")
                                    .foregroundStyle(.blue)
                            }
                        }
                        .padding()
                        .background(
                            RoundedRectangle(cornerRadius: 12)
                                .fill(selectedProfile == profile ? Color.blue.opacity(0.1) : Color(.systemGray6))
                        )
                    }
                    .foregroundStyle(.primary)
                }
            }
            .padding(.horizontal)
            
            Spacer()
            
            navigationButtons
        }
        .padding()
    }
    
    private var apiKeyPage: some View {
        VStack(spacing: 24) {
            Image(systemName: "sparkles")
                .font(.system(size: 60))
                .foregroundStyle(.purple)
            
            Text("AI-Powered Analysis")
                .font(.title)
                .fontWeight(.bold)
            
            Text("Add your Gemini API key to enable AI food analysis from photos and descriptions")
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            
            SecureField("Gemini API Key", text: $apiKey)
                .textFieldStyle(.roundedBorder)
                .padding(.horizontal)
            
            Link(destination: URL(string: "https://ai.google.dev/")!) {
                Label("Get API Key", systemImage: "arrow.up.right.square")
            }
            
            Text("You can add this later in Settings")
                .font(.caption)
                .foregroundStyle(.secondary)
            
            Spacer()
            
            navigationButtons
        }
        .padding()
    }
    
    private var healthPage: some View {
        VStack(spacing: 24) {
            Image(systemName: "heart.fill")
                .font(.system(size: 60))
                .foregroundStyle(.red)
            
            Text("Health Integration")
                .font(.title)
                .fontWeight(.bold)
            
            Text("Sync your food entries with the Health app for a complete nutrition overview")
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            
            if healthKitManager.isAvailable {
                Toggle("Enable HealthKit Sync", isOn: $enableHealthKit)
                    .padding()
                    .background(Color(.systemGray6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            } else {
                Text("HealthKit is not available on this device")
                    .foregroundStyle(.secondary)
            }
            
            Spacer()
            
            Button {
                completeOnboarding()
            } label: {
                Text("Complete Setup")
                    .font(.headline)
                    .frame(maxWidth: .infinity)
                    .padding()
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
    }
    
    private var navigationButtons: some View {
        HStack {
            if currentPage > 0 {
                Button {
                    withAnimation {
                        currentPage -= 1
                    }
                } label: {
                    Image(systemName: "arrow.left")
                        .padding()
                }
            }
            
            Spacer()
            
            Button {
                withAnimation {
                    currentPage += 1
                }
            } label: {
                Image(systemName: "arrow.right")
                    .padding()
            }
            .buttonStyle(.borderedProminent)
        }
        .padding(.horizontal)
    }
    
    private func completeOnboarding() {
        // Save API key if provided
        if !apiKey.isEmpty {
            settingsManager.geminiApiKey = apiKey
        }
        
        // Apply profile
        settingsManager.applyProfile(selectedProfile, weight: weight, height: height, age: age, isMale: isMale)
        
        // Enable HealthKit if selected
        settingsManager.healthKitEnabled = enableHealthKit
        if enableHealthKit {
            Task {
                try? await healthKitManager.requestAuthorization()
            }
        }
        
        // Mark onboarding complete
        settingsManager.completeOnboarding()
        
        withAnimation {
            isComplete = false
        }
    }
}

#Preview {
    OnboardingView(isComplete: .constant(true))
}
