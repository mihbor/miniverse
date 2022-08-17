package three.mesh.ui

fun BlockState(
    state: String,
    attributes: BlockProps,
    onSet: (() -> Unit)? = null
) = (js("{}") as BlockState).apply {
    this.state = state
    this.attributes = attributes
    onSet?.let { this.onSet = it }
}

fun BlockProps() = js("{}") as BlockProps

fun TextProps(content: String) = (js("{}") as TextProps).apply {
    this.content = content
}