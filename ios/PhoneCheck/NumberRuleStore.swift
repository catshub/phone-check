import Foundation

enum NumberRuleStore {
    private static let key = "number_rules"

    static func read() -> String {
        UserDefaults.standard.string(forKey: key) ?? ""
    }

    static func save(_ text: String) {
        UserDefaults.standard.set(text, forKey: key)
    }
}
