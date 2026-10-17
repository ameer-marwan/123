import SwiftUI
import UIKit

// Represents the shared Compose Multiplatform view hierarchy inside SwiftUI
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Calls the exported Kotlin/Native Compose Multiplatform entrypoint
        return MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Updates handled through reactive StateFlow in shared Kotlin layer
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}

#Preview {
    ContentView()
}
