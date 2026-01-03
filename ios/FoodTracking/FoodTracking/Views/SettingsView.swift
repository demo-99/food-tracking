// SettingsView - App settings and configuration

import SwiftUI

struct SettingsView: View {
    @StateObject private var settingsManager = SettingsManager()
    @StateObject private var healthKitManager = HealthKitManager()
    
    @State private var showApiKeyInput = false
    @State private var tempApiKey = ""
    
    var body: some View {
        NavigationStack {
            Form {
                // Gemini API Section
                Section {
                    if settingsManager.hasGeminiApiKey {
                        HStack {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundStyle(.green)
                            Text("API Key configured")
                            Spacer()
                            Button("Change") {
                                tempApiKey = settingsManager.geminiApiKey
                                showApiKeyInput = true
                            }
                        }
                    } else {
                        Button {
                            showApiKeyInput = true
                        } label: {
                            Label("Add Gemini API Key", systemImage: "key")
                        }
                    }
                } header: {
                    Text("AI Settings")
                } footer: {
                    Text("Required for AI-powered food analysis from photos and descriptions.")
                }
                
                // Nutrition Profile Section
                Section("Nutrition Profile") {
                    Picker("Profile", selection: $settingsManager.selectedProfile) {
                        ForEach(NutritionProfile.allCases) { profile in
                            Label(profile.rawValue, systemImage: profile.icon)
                                .tag(profile)
                        }
                    }
                    
                    if settingsManager.selectedProfile != .custom {
                        Text(settingsManager.selectedProfile.description)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                
                // Goals Section
                Section("Daily Goals") {
                    HStack {
                        Text("Calories")
                        Spacer()
                        TextField("kcal", value: $settingsManager.calorieGoal, format: .number)
                            .keyboardType(.numberPad)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 80)
                    }
                    
                    HStack {
                        Text("Protein")
                        Spacer()
                        TextField("g", value: $settingsManager.proteinGoal, format: .number)
                            .keyboardType(.numberPad)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 80)
                    }
                    
                    HStack {
                        Text("Carbs")
                        Spacer()
                        TextField("g", value: $settingsManager.carbsGoal, format: .number)
                            .keyboardType(.numberPad)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 80)
                    }
                    
                    HStack {
                        Text("Fat")
                        Spacer()
                        TextField("g", value: $settingsManager.fatsGoal, format: .number)
                            .keyboardType(.numberPad)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 80)
                    }
                }
                
                // HealthKit Section
                Section {
                    if healthKitManager.isAvailable {
                        Toggle("Sync with Health", isOn: $settingsManager.healthKitEnabled)
                            .onChange(of: settingsManager.healthKitEnabled) { _, newValue in
                                if newValue {
                                    Task {
                                        try? await healthKitManager.requestAuthorization()
                                    }
                                }
                            }
                        
                        if settingsManager.healthKitEnabled {
                            HStack {
                                Image(systemName: healthKitManager.isAuthorized ? "checkmark.circle.fill" : "exclamationmark.circle")
                                    .foregroundStyle(healthKitManager.isAuthorized ? .green : .orange)
                                Text(healthKitManager.isAuthorized ? "Connected" : "Authorization needed")
                            }
                        }
                    } else {
                        Text("HealthKit not available on this device")
                            .foregroundStyle(.secondary)
                    }
                } header: {
                    Text("Health Integration")
                } footer: {
                    Text("Sync your food entries to the Health app for a complete nutrition overview.")
                }
                
                // About Section
                Section("About") {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text("1.0.0")
                            .foregroundStyle(.secondary)
                    }
                    
                    Link(destination: URL(string: "https://ai.google.dev/")!) {
                        HStack {
                            Text("Get Gemini API Key")
                            Spacer()
                            Image(systemName: "arrow.up.right.square")
                        }
                    }
                }
            }
            .navigationTitle("Settings")
            .sheet(isPresented: $showApiKeyInput) {
                NavigationStack {
                    Form {
                        Section {
                            SecureField("API Key", text: $tempApiKey)
                        } footer: {
                            Text("Get your API key from Google AI Studio (ai.google.dev)")
                        }
                    }
                    .navigationTitle("Gemini API Key")
                    .navigationBarTitleDisplayMode(.inline)
                    .toolbar {
                        ToolbarItem(placement: .cancellationAction) {
                            Button("Cancel") {
                                showApiKeyInput = false
                            }
                        }
                        ToolbarItem(placement: .confirmationAction) {
                            Button("Save") {
                                settingsManager.geminiApiKey = tempApiKey
                                showApiKeyInput = false
                            }
                            .disabled(tempApiKey.isEmpty)
                        }
                    }
                }
                .presentationDetents([.medium])
            }
        }
    }
}

#Preview {
    SettingsView()
}
