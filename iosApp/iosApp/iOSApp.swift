import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        guard
            let environmentUrl = Bundle.main.object(
                forInfoDictionaryKey: "API_BASE_URL"
            ) as? String,
            !environmentUrl.isEmpty
        else {
            fatalError("API_BASE_URL is not configured")
        }

        KoinInitIosKt.startKoinIos(baseUrl: environmentUrl)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
