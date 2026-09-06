import SwiftUI

struct ContentView: View {
    @State private var rulesText = NumberRuleStore.read()
    @State private var updateMessage = "未检查更新"

    var body: some View {
        NavigationStack {
            Form {
                Section("号码转化规则") {
                    Text("每行一条：前缀=>替换内容，例如 116451=>")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                    TextEditor(text: $rulesText)
                        .frame(minHeight: 120)
                    Button("保存规则") {
                        NumberRuleStore.save(rulesText)
                    }
                }

                Section("更新") {
                    Text(updateMessage)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                    Button("检查更新") {
                        Task {
                            updateMessage = "检查中..."
                            updateMessage = await UpdateChecker.check().message
                        }
                    }
                }

                Section("说明") {
                    Text("iOS 系统限制比 Android 更严格，本版本提供号码规则和更新检查；来电身份识别需要通过 CallKit 另行接入并受系统审核限制。")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
            }
            .navigationTitle("来电身份识别")
        }
    }
}
