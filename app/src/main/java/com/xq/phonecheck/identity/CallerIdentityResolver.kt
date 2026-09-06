package com.xq.phonecheck.identity

import android.content.Context

object CallerIdentityResolver {
    fun resolve(context: Context, rawNumber: String?): CallerIdentity {
        val number = PhoneNumbers.normalize(rawNumber)
        if (number.isEmpty()) {
            return CallerIdentity(
                number = "",
                source = IdentitySource.UNKNOWN,
                reason = "系统未提供来电号码，无法识别"
            )
        }

        val transformedNumber = NumberRuleStore.apply(context, number)
        if (transformedNumber.isEmpty()) {
            return CallerIdentity(
                number = number,
                source = IdentitySource.UNKNOWN,
                reason = "号码转化后为空，请检查自定义规则"
            )
        }

        return ContactDirectory.lookup(context, transformedNumber)
            ?: UserDirectory.lookup(context, transformedNumber)
            ?: CallerIdentity(
                number = transformedNumber,
                source = IdentitySource.UNKNOWN,
                reason = "未匹配到本机通讯录或授权资料库；无可靠微信账号信息，不能确认对方身份"
            )
    }
}
