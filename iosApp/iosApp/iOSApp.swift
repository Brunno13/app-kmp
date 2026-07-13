import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
            #if DEBUG
            let environmentUrl = "http://api-bun-staging.brunnoserver.duckdns.org/"
            #else
            let environmentUrl = "https://api-bun.brunnoserver.duckdns.org/"
            #endif

            KoinModuleKt.initKoin(baseUrl: environmentUrl, appDeclaration: { _ in })
        }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}