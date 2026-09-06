import Foundation

struct GitHubRelease: Decodable {
    let tagName: String
    let htmlURL: String

    enum CodingKeys: String, CodingKey {
        case tagName = "tag_name"
        case htmlURL = "html_url"
    }
}

struct UpdateCheckResult {
    let message: String
    let releaseURL: URL?
}

enum UpdateChecker {
    private static let releaseURL = URL(string: "https://api.github.com/repos/catshub/phone-check/releases/latest")!

    static func check() async -> UpdateCheckResult {
        var request = URLRequest(url: releaseURL)
        request.setValue("application/vnd.github+json", forHTTPHeaderField: "Accept")
        request.setValue("phone-check-ios", forHTTPHeaderField: "User-Agent")

        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let http = response as? HTTPURLResponse, http.statusCode == 200 else {
                return UpdateCheckResult(message: "GitHub 没有发布信息", releaseURL: nil)
            }

            let release = try JSONDecoder().decode(GitHubRelease.self, from: data)
            let latest = release.tagName.replacingOccurrences(of: "v", with: "")
            let current = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? ""
            let message = isNewer(latest, current)
                ? "发现新版本 \(latest)"
                : "已是最新版本"
            return UpdateCheckResult(message: message, releaseURL: URL(string: release.htmlURL))
        } catch {
            return UpdateCheckResult(message: "检查更新失败", releaseURL: nil)
        }
    }

    private static func isNewer(_ latest: String, _ current: String) -> Bool {
        let latestParts = latest.split(separator: ".").map { Int($0) ?? 0 }
        let currentParts = current.split(separator: ".").map { Int($0) ?? 0 }
        let count = max(latestParts.count, currentParts.count)

        for index in 0..<count {
            let left = index < latestParts.count ? latestParts[index] : 0
            let right = index < currentParts.count ? currentParts[index] : 0
            if left != right { return left > right }
        }
        return false
    }
}
