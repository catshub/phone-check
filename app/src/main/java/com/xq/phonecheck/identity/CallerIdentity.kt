package com.xq.phonecheck.identity

enum class IdentitySource {
    CONTACT,
    CONTACT_WITH_WECHAT,
    USER_DIRECTORY,
    UNKNOWN
}

data class CallerIdentity(
    val number: String,
    val source: IdentitySource,
    val displayName: String? = null,
    val wechatId: String? = null,
    val note: String? = null,
    val reason: String
) {
    val trusted: Boolean
        get() = source != IdentitySource.UNKNOWN

    val shortLabel: String
        get() = when (source) {
            IdentitySource.CONTACT_WITH_WECHAT -> "已知联系人（含微信资料）"
            IdentitySource.CONTACT -> "已保存联系人"
            IdentitySource.USER_DIRECTORY -> "本机号码资料库"
            IdentitySource.UNKNOWN -> "疑似骚扰"
        }
}
