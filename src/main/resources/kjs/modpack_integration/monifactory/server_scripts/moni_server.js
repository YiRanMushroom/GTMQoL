ServerEvents.recipes(event => {
    event.recipes.gtceu.assembler('test')
        .itemInputs(
            '64x minecraft:dirt',
            '32x minecraft:diamond'
        )
        .inputFluids(
            Fluid.of('minecraft:water', 1500)
        )
        .itemOutputs(
            'minecraft:stick'
        )
        .duration(100)
        .EUt(30)
})