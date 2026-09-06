import Foundation

struct NumberRule: Identifiable {
    let id = UUID()
    let prefix: String
    let replacement: String
}

enum NumberRules {
    static func normalize(_ raw: String?) -> String {
        guard let raw, !raw.isEmpty else { return "" }
        let digits = raw.filter(\.isNumber)
        if digits.isEmpty { return "" }

        if digits.hasPrefix("0086") { return String(digits.dropFirst(4)) }
        if digits.count == 13 && digits.hasPrefix("86") { return String(digits.dropFirst(2)) }
        return digits
    }

    static func parse(_ text: String) -> [NumberRule] {
        text
            .split(separator: "\n")
            .map { $0.trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty && !$0.hasPrefix("#") }
            .compactMap { line in
                let parts = line
                    .split(separator: "=>", maxSplits: 1, omittingEmptySubsequences: false)
                    .map { String($0) }
                let prefix = normalize(parts.first)
                guard !prefix.isEmpty else { return nil }
                let replacement = parts.count > 1 ? parts[1].trimmingCharacters(in: .whitespaces) : ""
                return NumberRule(prefix: prefix, replacement: replacement)
            }
            .sorted { $0.prefix.count > $1.prefix.count }
    }

    static func apply(_ rawNumber: String, rulesText: String) -> String {
        let number = normalize(rawNumber)
        guard let rule = parse(rulesText).first(where: { number.hasPrefix($0.prefix) }) else {
            return number
        }
        let remainder = number.dropFirst(rule.prefix.count)
        if rule.replacement.isEmpty && remainder.isEmpty { return number }
        return rule.replacement + remainder
    }
}
