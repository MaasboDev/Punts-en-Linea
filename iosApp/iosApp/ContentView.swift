import SwiftUI
import SharedLogic

struct ContentView: View {
    @Environment(\.colorScheme) private var colorScheme
    @State private var showContent = true

    private let telemetry: TelemetryCoordinator

    init(telemetry: TelemetryCoordinator = TelemetryRegistry.shared.coordinator) {
        self.telemetry = telemetry
    }

    private var isDark: Bool {
        colorScheme == .dark
    }

    // Design tokens matching AGENTS.md & NFR-3:
    // Primary: #B57850 (Light) / #E5A982 (Dark)
    private var primaryColor: Color {
        isDark
            ? Color(red: 0xE5 / 255.0, green: 0xA9 / 255.0, blue: 0x82 / 255.0)
            : Color(red: 0xB5 / 255.0, green: 0x78 / 255.0, blue: 0x50 / 255.0)
    }

    // OnPrimary: #FFFFFF (Light) / #44230D (Dark)
    private var onPrimaryColor: Color {
        isDark
            ? Color(red: 0x44 / 255.0, green: 0x23 / 255.0, blue: 0x0D / 255.0)
            : Color(red: 0xFF / 255.0, green: 0xFF / 255.0, blue: 0xFF / 255.0)
    }

    // Background: #F4F5F7 (Light) / #181412 (Dark)
    private var backgroundColor: Color {
        isDark
            ? Color(red: 0x18 / 255.0, green: 0x14 / 255.0, blue: 0x12 / 255.0)
            : Color(red: 0xF4 / 255.0, green: 0xF5 / 255.0, blue: 0xF7 / 255.0)
    }

    // OnBackground: #241E1A (Light) / #EDE4DF (Dark)
    private var onBackgroundColor: Color {
        isDark
            ? Color(red: 0xED / 255.0, green: 0xE4 / 255.0, blue: 0xDF / 255.0)
            : Color(red: 0x24 / 255.0, green: 0x1E / 255.0, blue: 0x1A / 255.0)
    }

    var body: some View {
        VStack(spacing: 16) {
            Button(action: {
                withAnimation {
                    showContent = !showContent
                }
            }) {
                Text("toggle_content_button", tableName: "Localizable")
                    .foregroundStyle(onPrimaryColor)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(
                        RoundedRectangle(cornerRadius: 16, style: .continuous)
                            .fill(primaryColor)
                    )
            }
            .buttonStyle(.plain)

            if showContent {
                VStack(spacing: 16) {
                    Image("BrandLogo")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 200, height: 200)
                        .accessibilityLabel(Text("brand_logo_content_description", tableName: "Localizable"))
                    Text(String(format: String(localized: "greeting_prefix"), Greeting().greet()))
                        .foregroundStyle(onBackgroundColor)
                }
                .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .padding()
        .background(backgroundColor.ignoresSafeArea())
        .onAppear {
            telemetry.logScreenView(
                screenName: "main_screen",
                screenClass: "ContentView"
            )
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

