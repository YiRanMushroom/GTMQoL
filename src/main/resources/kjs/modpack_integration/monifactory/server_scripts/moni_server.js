ServerEvents.recipes(event => {
    console.info('[GTMQoL] Triggering server recipe event...')

    // Controller
    event.recipes.gtceu.assembly_line("omni_prismatic_crucible")
        .itemInputs("8x monilabs:prismatic_crucible", "16x gtceu:uhv_field_generator", "64x #gtceu:circuits/uhv", "64x monilabs:prism_glass", "32x gtceu:dense_trinaquadalloy_plate", "64x gtceu:normal_laser_pipe", "48x gtceu:tritanium_single_cable")
        .inputFluids("gtceu:living_soldering_alloy 4608", "gtceu:polyether_ether_ketone 4608", "gtceu:omnium 110592")
        .itemOutputs("gtceu:omni_prismatic_crucible")
        .duration(20 * 240)
        .EUt(GTValues.VA[GTValues.UEV])
        .stationResearch(b => b
            .researchStack("monilabs:prismatic_crucible")
            .CWUt(192)
            .EUt(GTValues.VA[GTValues.UEV])
        )
})