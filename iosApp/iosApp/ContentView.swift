import SwiftUI
import shared

/**
 * SwiftUI wrapper for the Compose Multiplatform UI.
 * Uses UIViewControllerRepresentable to embed the Compose UIViewController.
 */
struct ComposeView: UIViewControllerRepresentable {
    
    /**
     * Creates the Compose UIViewController from the shared Kotlin module.
     */
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    
    /**
     * No updates needed - Compose handles its own state management.
     */
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Compose handles all UI updates internally
    }
}

/**
 * Main content view that displays the Compose UI.
 */
struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard) // Allow Compose to handle keyboard
    }
}

#Preview {
    ContentView()
}
