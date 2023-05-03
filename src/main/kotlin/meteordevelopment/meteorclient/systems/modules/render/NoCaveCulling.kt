package meteordevelopment.meteorclient.systems.modules.render

import meteordevelopment.meteorclient.systems.*
import meteordevelopment.meteorclient.systems.modules.Categories

class NoCaveCulling: LemonModule(Categories.Render, "NoCaveCulling", "Disables Minecraft's cave culling algorithm.") {

    override fun onActivate() {
        super.onActivate()
        mc.chunkCullingEnabled = false
        mc.worldRenderer.reload()
    }

    override fun onDeactivate() {
        super.onDeactivate()
        mc.chunkCullingEnabled = true
        mc.worldRenderer.reload()
    }

}
