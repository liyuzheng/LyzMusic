package yz.li.core_tools.exts

/**
 * desc:
 * createed by liyuzheng on 2023/8/24 20:43
 */
inline fun <T, R> T.transform(block: (T) -> R): R? {
    return runCatching {
        block(this)
    }.getOrNull()
}
