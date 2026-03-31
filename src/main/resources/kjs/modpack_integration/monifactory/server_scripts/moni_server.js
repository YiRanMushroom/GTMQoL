ServerEvents.recipes(event => {
    console.info('[GTMQoL] Triggering server recipe event...')

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

    event.recipes.gtceu.assembly_line("moni_eye_of_harmony")
        .itemInputs(
            "4x monilabs:hyperbolic_microverse_projector",
            "16x gtceu:uv_field_generator",
            "64x #gtceu:circuits/uv", "16x kubejs:heart_of_a_universe", "64x monilabs:double_transcendental_matrix_plate", "4x kubejs:supercritical_prismatic_core",
            "16x gtceu:omnium_hex_wire"
        )
        .inputFluids("gtceu:living_soldering_alloy 4608", "gtceu:polyether_ether_ketone 4608", "gtceu:omnium 110592")
        .itemOutputs("gtceu:moni_eye_of_harmony")
        .duration(20 * 240)
        .EUt(GTValues.VA[GTValues.UHV])
        .stationResearch(b => b
            .researchStack("monilabs:hyperbolic_microverse_projector")
            .CWUt(192)
            .EUt(GTValues.VA[GTValues.UHV])
        )

    event.recipes.gtceu.assembly_line("sculk_barrel")
        .itemInputs(
            "4x monilabs:sculk_vat",
            "16x gtceu:zpm_field_generator",
            "64x #gtceu:circuits/uv", "4x kubejs:heart_of_a_universe", "16x monilabs:double_transcendental_matrix_plate", "1x kubejs:supercritical_prismatic_core",
            "16x gtceu:omnium_octal_wire"
        )
        .inputFluids("gtceu:living_soldering_alloy 4608", "gtceu:polyether_ether_ketone 4608", "gtceu:omnium 110592")
        .itemOutputs("gtceu:sculk_barrel")
        .duration(20 * 240)
        .EUt(GTValues.VA[GTValues.UV])
        .stationResearch(b => b
            .researchStack("monilabs:sculk_vat")
            .CWUt(192)
            .EUt(GTValues.VA[GTValues.UV])
        )

    if (true) {
        event.recipes.gtceu.compressor("easy_omni_prismatic_crucible")
            .itemInputs("16x monilabs:prismatic_crucible")
            .itemOutputs("gtceu:omni_prismatic_crucible")
            .duration(20 * 120 * 600)
            .EUt(GTValues.VA[GTValues.EV])

        event.recipes.gtceu.compressor("easy_moni_eye_of_harmony")
            .itemInputs("16x monilabs:hyperbolic_microverse_projector")
            .itemOutputs("gtceu:moni_eye_of_harmony")
            .duration(20 * 120 * 600)
            .EUt(GTValues.VA[GTValues.EV])

        event.recipes.gtceu.compressor("easy_sculk_barrel")
            .itemInputs("16x monilabs:sculk_vat")
            .itemOutputs("gtceu:sculk_barrel")
            .duration(20 * 120 * 600)
            .EUt(GTValues.VA[GTValues.EV])
    }
})