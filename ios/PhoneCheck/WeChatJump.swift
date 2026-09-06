import UIKit

enum WeChatJump {
    private static let scheme = "weixin://"

    static func open() {
        guard let url = URL(string: scheme), UIApplication.shared.canOpenURL(url) else { return }
        UIApplication.shared.open(url)
    }
}
