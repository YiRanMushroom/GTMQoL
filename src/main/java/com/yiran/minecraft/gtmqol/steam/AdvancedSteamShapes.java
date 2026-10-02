package com.yiran.minecraft.gtmqol.steam;

/**
 * Structures of the advanced steam multiblocks, converted from GTNL's large steam multiblocks with every block
 * replaced by a bronze one. One array per slice from the front, strings from the bottom up, for
 * {@code MultiblockPatternBuilder.start()}. Symbols are mapped in {@link AdvancedSteamMachines}:
 * <ul>
 * <li>{@code S} controller, {@code X} bronze plated bricks or a hatch, space any block</li>
 * <li>{@code G} bronze gearbox, {@code P} bronze pipe casing, {@code F} bronze frame, {@code B} bronze firebox</li>
 * <li>{@code H} bronze hull, {@code K} bronze brick hull, {@code Z} bronze block</li>
 * <li>{@code L} glass, {@code I} iron block, {@code D} diamond block, {@code T} stone bricks</li>
 * </ul>
 * The assembler and the magical assembler use GTNL's steam manufacturer.
 */
final class AdvancedSteamShapes {

    static final String[][] MACERATOR = {
            { "KKKKKKK", "XXXSXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "XXXXXXX", "       ", "       " },
            { "KKKKKKK", "X     X", "XFFFFFX", "X     X", "XGGGGGX", "XFFFFFX", "X     X", "XX   XX" },
            { "KKKKKKK", "X     X", "XGGGGGX", "XFFFFFX", "X     X", "X     X", "X     X", "XXXXXXX" },
            { "KKKKKKK", "X     X", "XFFFFFX", "X     X", "XGGGGGX", "XFFFFFX", "XXXXXXX", "XXXXXXX" },
            { "KKKKKKK", "X     X", "XGGGGGX", "XFFFFFX", "X     X", "X     X", "XXXXXXX", "FXXXXXF" },
            { "KKKKKKK", "X     X", "XFFFFFX", "X     X", "XXXXXXX", "XXXXXXX", " XXXXX ", "F     F" },
            { "KKKKKKK", "X     X", "XGGGGGX", "X     X", "XXXXXXX", " XXXXX ", "       ", "F     F" },
            { "KKKKKKK", "X     X", "XXXXXXX", "XXXXXXX", " XXXXX ", "  XXX  ", "       ", "F     F" },
            { "KKKKKKK", "X     X", "XXXXXXX", "  X X  ", "  X X  ", "  XXX  ", "       ", "F     F" },
            { "KKKKKKK", "X     X", "XXXXXXX", "  XXX  ", "  XXX  ", "  XXX  ", "       ", "F     F" },
            { "KKKKKKK", "XFFFFFX", "XXXXXXX", "F     F", "F     F", "F     F", "F     F", "FFFFFFF" },
    };

    static final String[][] COMPRESSOR = {
            { "  XXX  ", "  XSX  ", "       ", "       ", "       ", "  XXX  ", "  XXX  " },
            { " XXXXX ", " XGGGX ", " FLLLF ", " FLLLF ", " FLLLF ", " XGGGX ", " XXXXX " },
            { "XXXXXXX", "XGIIIGX", " L   L ", " L   L ", " L   L ", "XGIIIGX", "XXXXXXX" },
            { "XXXXXXX", "XGIIIGX", " L   L ", " L   L ", " L   L ", "XGIIIGX", "XXXXXXX" },
            { "XXXXXXX", "XGIIIGX", " L   L ", " L   L ", " L   L ", "XGIIIGX", "XXXXXXX" },
            { " XXXXX ", " XGGGX ", " FLLLF ", " FLLLF ", " FLLLF ", " XGGGX ", " XXXXX " },
            { "  XXX  ", "  XXX  ", "       ", "       ", "       ", "  XXX  ", "  XXX  " },
    };

    static final String[][] FORGE_HAMMER = {
            { "  XXX  ", "  XSX  ", "   X   ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "   X   ", "  XXX  ", "  XXX  " },
            { " XXXXX ", " XXIXX ", " FXXXF ", " FXFXF ", " F   F ", " F   F ", " F   F ", " F   F ", " F   F ", " FXFXF ", " FXXXF ", " XXIXX ", " XXXXX " },
            { "XXXXXXX", "XXIIIXX", " XIIIX ", " XGLGX ", "  GLG  ", "  GLG  ", "  GLG  ", "  GLG  ", "  GLG  ", " XGLGX ", " XIIIX ", "XXIIIXX", "XXXXXXX" },
            { "XXXXXXX", "XIIIIIX", "XXIIIXX", " FL LF ", "  L L  ", "  L L  ", "  L L  ", "  L L  ", "  LIL  ", " FLILF ", "XXIIIXX", "XIIIIIX", "XXXXXXX" },
            { "XXXXXXX", "XXIIIXX", " XIIIX ", " XGLGX ", "  GLG  ", "  GLG  ", "  GLG  ", "  GLG  ", "  GLG  ", " XGLGX ", " XIIIX ", "XXIIIXX", "XXXXXXX" },
            { " XXXXX ", " XXIXX ", " FXXXF ", " FXFXF ", " F   F ", " F   F ", " F   F ", " F   F ", " F   F ", " FXFXF ", " FXXXF ", " XXIXX ", " XXXXX " },
            { "  XXX  ", "  XXX  ", "   X   ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "   X   ", "  XXX  ", "  XXX  " },
    };

    static final String[][] EXTRACTOR = {
            { "F   F", "XXSXX", "XGGGX", "XXXXX", " XXX " },
            { "F   F", "XXFXX", "XGGGX", "XP PX", "XX XX" },
            { "F   F", "XXFXX", "XGGGX", "XP PX", "XX XX" },
            { "F   F", "XXFXX", "XGGGX", "XP PX", "XX XX" },
            { "F   F", "XXXXX", "XGGGX", "XXXXX", " XXX " },
    };

    static final String[][] ALLOY_SMELTER = {
            { "BBB", "XSX", "XXX", " X " },
            { "BBB", "X X", "XXX", "XXX" },
            { "BBB", "XXX", "XXX", " X " },
    };

    static final String[][] FURNACE = {
            { "F    F   ", "F    F   ", "F    F   ", "F    F   ", "F    F   ", " FFFF    ", "         ", "         " },
            { "KKKKKKXXX", "XBBBBXXSX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXX   ", "FXXXXF   ", "  XX     ", "  XX     " },
            { "KTTTTKHHH", "X    XHFH", "X PP XHPH", "X FF XHHH", "X    XHHH", "FXPPXF   ", " X  X    ", " X  X    " },
            { "KTTTTKHHH", "B    XHFH", "X PP XHPH", "X FF XH H", "X    XHFH", "FXPPXF   ", " X  X    ", " X  X    " },
            { "KTTTTKHHH", "B    XHFH", "X PP XHPH", "X FF XH H", "X    XHFH", "FXXXXF   ", "  XX     ", "  XX     " },
            { "KTTTTKHHH", "B    XHFH", "X PP XHPH", "X FF XH H", "X    XHFH", "FXXXXF   ", "  XX     ", "  XX     " },
            { "KTTTTKHHH", "B    XHFH", "X PP XHPH", "X FF XH H", "X    XHFH", "FXPPXF   ", " X  X    ", " X  X    " },
            { "KTTTTKHHH", "X    XHFH", "X PP XHPH", "X FF XHHH", "X    XHHH", "FXPPXF   ", " X  X    ", " X  X    " },
            { "KKKKKKXXX", "XXXXXXXXX", "XBBBBXXXX", "XXXXXXXXX", "XXXXXX   ", "FXXXXF   ", "  XX     ", "  XX     " },
            { "F    F   ", "F    F   ", "F    F   ", "F    F   ", "F    F   ", " FFFF    ", "         ", "         " },
    };

    static final String[][] BENDER = {
            { "FXXXF", "F S F", "F   F", "     " },
            { "FXXXF", "     ", "F   F", "     " },
            { "FXXXF", "     ", "FXXXF", "     " },
            { "FXXXF", "FXXXF", "GPPPG", "FXXXF" },
            { "FXXXF", "F   F", "FXXXF", "F   F" },
    };

    static final String[][] WIREMILL = {
            { "XXXXXF", "XSXF F", " X F F", "   F F", "   F F" },
            { "XXXXX ", "LPX   ", "XXXXXX", "   XPX", "   XXX" },
            { "XXXXX ", "LPX   ", "XXXXXX", "   XPX", "   XXX" },
            { "XXXXX ", "LPX   ", "XXXXXX", "   XPX", "   XXX" },
            { "XXXXXF", "XXXF F", " X F F", "   F F", "   F F" },
    };

    static final String[][] LATHE = {
            { " XXXXX ", "  XSX  ", "       ", "       " },
            { "XHHHHHX", "XHPPPHX", " HLLLH ", " FHHHF " },
            { "XHHHHHX", "XH   HX", "XGIIIGX", "XHHHHHX" },
            { "XHHHHHX", "XHPPPHX", " HLLLH ", " FHHHF " },
            { " XXXXX ", "  XXX  ", "       ", "       " },
    };

    static final String[][] CUTTER = {
            { " HXXXXXH ", " HXXSXXH ", "  HFFFH  ", "   XXX   " },
            { "HHKKKKKHH", "HHH   HHH", " HF G FH ", "  XHHHX  " },
            { "HHKKKKKHH", "HPPPPPPPH", " HFDDDFH ", "  XHHHX  " },
            { "HHKKKKKHH", "HHH   HHH", " HF G FH ", "  XHHHX  " },
            { " HXXXXXH ", " HXXXXXH ", "  HFFFH  ", "   XXX   " },
    };

    static final String[][] EXTRUDER = {
            { "XXSXX", "XLLLX", "XLLLX", "XLLLX", "XXXXX", "FFFFF", "     ", "     " },
            { "XKKKX", "LIIIL", "LIIIL", "LIIIL", "XPPPX", "F   F", "     ", " XXX " },
            { "XKKKX", "LIIIL", "LIPIL", "LIPIL", "XPGPX", "X G X", "X G X", "XXGXX" },
            { "XKKKX", "LIIIL", "LIIIL", "LIIIL", "XPPPX", "F   F", "     ", " XXX " },
            { "XXXXX", "XLLLX", "XLLLX", "XLLLX", "XXXXX", "FFFFF", "     ", "     " },
    };

    static final String[][] FORMING_PRESS = {
            { " XSX ", " X X ", " XXX " },
            { "XXXXX", "XGPGX", "XXXXX" },
            { "XXXXX", " P P ", "XXXXX" },
            { "XXXXX", "XGPGX", "XXXXX" },
            { " XXX ", " X X ", " XXX " },
    };

    static final String[][] MIXER = {
            { "  BBB  ", "  LXL  ", "  LXL  ", "  LSL  ", "  LXL  ", "  LXL  ", "  BBB  " },
            { " BXXXB ", " XHGHX ", " XHGHX ", " XHGHX ", " XHGHX ", " XHGHX ", " BXXXB " },
            { "BXXXXXB", "LH P HL", "LHPPPHL", "LHG GHL", "LHPPPHL", "LH   HL", "BXXXXXB" },
            { "BXXXXXB", "XGPPPGX", "XGPPPGX", "XG G GX", "XGPPPGX", "XG P GX", "BXXXXXB" },
            { "BXXXXXB", "LH P HL", "LHPPPHL", "LHG GHL", "LHPPPHL", "LH   HL", "BXXXXXB" },
            { " BXXXB ", " XHGHX ", " XHGHX ", " XHGHX ", " XHGHX ", " XHGHX ", " BXXXB " },
            { "  BBB  ", "  LXL  ", "  LXL  ", "  LXL  ", "  LXL  ", "  LXL  ", "  BBB  " },
    };

    static final String[][] CENTRIFUGE = {
            { "  XSX  ", "  XLX  ", "  XLX  ", "  XLX  ", "  XLX  ", "  XLX  ", "  XLX  ", "  XLX  ", "  XXX  ", "       " },
            { " XXXXX ", " FP PF ", " FG GF ", " FP PF ", " FG GF ", " FP PF ", " FG GF ", " FP PF ", " XXXXX ", "  XXX  " },
            { "XXXXXXX", "XP   PX", "XG   GX", "XP   PX", "XG   GX", "XP   PX", "XG   GX", "XP   PX", "XX   XX", " XXXXX " },
            { "XXXXXXX", "L     L", "L     L", "L     L", "L     L", "L     L", "L     L", "L     L", "X     X", " XXGXX " },
            { "XXXXXXX", "XP   PX", "XG   GX", "XP   PX", "XG   GX", "XP   PX", "XG   GX", "XP   PX", "XX   XX", " XXXXX " },
            { " XXXXX ", " FP PF ", " FG GF ", " FP PF ", " FG GF ", " FP PF ", " FG GF ", " FP PF ", " XXXXX ", "  XXX  " },
            { "  XXX  ", "  XLX  ", "  XLX  ", "  XLX  ", "  XLX  ", "  XLX  ", "  XLX  ", "  XLX  ", "  XXX  ", "       " },
    };

    static final String[][] THERMAL_CENTRIFUGE = {
            { " BBBBB ", " XXXXX ", " XXSXX ", " XXXXX ", "       " },
            { "BXXBXXB", "XF   FX", "XF   FX", "XF   FX", " XXXXX " },
            { "BXBBBXB", "X     X", "X     X", "X     X", " XXXXX " },
            { "BBBBBBB", "X  P  X", "X  P  X", "X  P  X", " XXXXX " },
            { "BXBBBXB", "X     X", "X     X", "X     X", " XXXXX " },
            { "BXXBXXB", "XF   FX", "XF   FX", "XF   FX", " XXXXX " },
            { " BBBBB ", " XXXXX ", " XXXXX ", " XXXXX ", "       " },
    };

    static final String[][] ORE_WASHER = {
            { "XXXXSXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX" },
            { "XXXXXXXXX", "X   P   X", "X       X", "X       X", "XLLLLLLLX" },
            { "XXXXXXXXX", "X   P   X", "X       X", "X       X", "XLLLLLLLX" },
            { "XXXXXXXXX", "X   P   X", "X   P   X", "X       X", "XLLLLLLLX" },
            { "XXXXXXXXX", "XPPPPPPPX", "X  PPP  X", "X       X", "XLLLLLLLX" },
            { "XXXXXXXXX", "X   P   X", "X   P   X", "X       X", "XLLLLLLLX" },
            { "XXXXXXXXX", "X   P   X", "X       X", "X       X", "XLLLLLLLX" },
            { "XXXXXXXXX", "X   P   X", "X       X", "X       X", "XLLLLLLLX" },
            { "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX" },
    };

    static final String[][] CHEMICAL_BATH = {
            { "XXXXXXXXX", "XXXXXXXXX", "XXXXSXXXX", "XXXXXXXXX", "XXXXXXXXX" },
            { "XXXXXXXXX", "XFFFFFFFX", "XFFFZFFFX", "XFFFFFFFX", "XXXXXXXXX" },
            { "XXXXXXXXX", "XF     FX", "XF  Z  FX", "XF     FX", "XXLLLLLXX" },
            { "XXXXXXXXX", "XF     FX", "X   Z   X", "XF     FX", "XXLLLLLXX" },
            { "XXXXXXXXX", "XF     FX", "X   Z   X", "XF     FX", "XXLLLLLXX" },
            { "XXXXXXXXX", "XF     FX", "X   Z   X", "XF     FX", "XXLLLLLXX" },
            { "XXXXXXXXX", "XF     FX", "X   Z   X", "XF     FX", "XXLLLLLXX" },
            { "XXXXXXXXX", "XF     FX", "XF  Z  FX", "XF     FX", "XXLLLLLXX" },
            { "XXXXXXXXX", "XFFFFFFFX", "XFFFZFFFX", "XFFFFFFFX", "XXXXXXXXX" },
            { "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX", "XXXXXXXXX" },
    };

    static final String[][] SIFTER = {
            { " XXX ", " XSX ", " XXX ", " X X ", " X X ", " X X ", " FFF " },
            { "XXXXX", "XXGXX", "XXXXX", "XXLXX", "XXLXX", "XXLXX", "F X F" },
            { "XXXXX", "XGGGX", "XXGXX", " LGL ", " LGL ", " LGL ", "FXXXF" },
            { "XXXXX", "XXGXX", "XXXXX", "XXLXX", "XXLXX", "XXLXX", "F X F" },
            { " XXX ", " XXX ", " XXX ", " X X ", " X X ", " X X ", " FFF " },
    };

    static final String[][] CIRCUIT_ASSEMBLER = {
            { "XXX", "XSX", "XXX", " X " },
            { "XXX", "XPX", "XHX", " X " },
            { "XXX", "XPX", "XHX", " X " },
            { "XXX", "XPX", "XHX", " X " },
            { "XXX", "XPX", "XHX", " X " },
            { "XXX", "XPX", "XHX", " X " },
            { "XXX", "XPX", "XHX", " X " },
            { "XXX", "XPX", "XHX", " X " },
            { "XXX", "XPX", "XHX", " X " },
            { "XXX", "XXX", "XXX", " X " },
    };

    static final String[][] MANUFACTURER = {
            { " XXXXX   ", " GGSGG   ", " XXXXX   ", "         ", "         ", "         ", "         " },
            { "XXXXXXX  ", "G     G  ", "X     X  ", "         ", "         ", "         ", "         " },
            { "XXXXXXXXX", "G FFF FH ", "X     XH ", "       H ", "       H ", "     HHPH", "       H " },
            { "XXXXXXXXX", "G F FFFFX", "X     XFX", "   P   FX", "   P   FX", "   PPPPPX", "    HHHXH" },
            { "XXXXXXXXX", "G FFF FH ", "X     XH ", "       H ", "       H ", "     HHPH", "       H " },
            { "XXXXXXX  ", "G     G  ", "X     X  ", "         ", "         ", "         ", "         " },
            { " XXXXX   ", " GGGGG   ", " XXXXX   ", "         ", "         ", "         ", "         " },
    };

    private AdvancedSteamShapes() {}
}
