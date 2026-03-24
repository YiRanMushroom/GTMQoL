ClientEvents.lang('en_us', event => {
    console.info('[GTMQoL] Triggering en_us lang event...')
    event.add('block.gtceu.omni_prismatic_crucible', 'Omni Prismatic Crucible')
    event.add('gtceu.omni_prismatic_recipe', 'Omni Prismatic Synthesis')
    console.info('[GTMQoL] Added en_us translation for Omni Prismatic Crucible')
})

ClientEvents.lang('zh_cn', event => {
    console.info('[GTMQoL] Triggering zh_cn lang event...')
    event.add('block.gtceu.omni_prismatic_crucible', '全能棱彩坩埚')
    event.add('gtceu.omni_prismatic_recipe', '全能棱彩合成')
    console.info('[GTMQoL] Added zh_cn translation for Omni Prismatic Crucible')
})