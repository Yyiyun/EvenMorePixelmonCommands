package rs.expand.pixelupgrade.commands;

import com.pixelmonmod.pixelmon.enums.EnumType;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import rs.expand.pixelupgrade.PixelUpgrade;
import rs.expand.pixelupgrade.configs.WeaknessConfig;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static com.pixelmonmod.pixelmon.enums.EnumType.getTotalEffectiveness;
import static rs.expand.pixelupgrade.commands.Weakness.EnumPokemonList.getPokemonFromName;
import static rs.expand.pixelupgrade.commands.Weakness.EnumPokemonList.getPokemonFromID;

/*                                                *\
        HEAVILY WORK IN PROGRESS, STAY TUNED
\*                                                */

public class Weakness implements CommandExecutor
{
    // See which messages should be printed by the debug logger. Valid range is 0-3.
    // We set 4 (out of range) or null on hitting an error, and let the main code block handle it from there.
    private static Integer debugLevel = 4;
    private void getVerbosityMode()
    {
        // Does the debugVerbosityMode node exist? If so, figure out what's in it.
        if (!WeaknessConfig.getInstance().getConfig().getNode("debugVerbosityMode").isVirtual())
        {
            String modeString = WeaknessConfig.getInstance().getConfig().getNode("debugVerbosityMode").getString();

            if (modeString.matches("^[0-3]"))
                debugLevel = Integer.parseInt(modeString);
            else
                PixelUpgrade.log.info("\u00A74Weakness // critical: \u00A7cInvalid value on config variable \"debugVerbosityMode\"! Valid range: 0-3");
        }
        else
        {
            PixelUpgrade.log.info("\u00A74Weakness // critical: \u00A7cConfig variable \"debugVerbosityMode\" could not be found!");
            debugLevel = null;
        }
    }

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        if (src instanceof Player)
        {
            Integer commandCost = null;
            if (!WeaknessConfig.getInstance().getConfig().getNode("commandCost").isVirtual())
                commandCost = WeaknessConfig.getInstance().getConfig().getNode("commandCost").getInt();
            else
                PixelUpgrade.log.info("\u00A74Weakness // critical: \u00A7cCould not parse config variable \"commandCost\"!");

            // Check the command's debug verbosity mode, as set in the config.
            getVerbosityMode();

            if (commandCost == null || debugLevel == null || debugLevel >= 4 || debugLevel < 0)
            {
                // Specific errors are already called earlier on -- this is tacked on to the end.
                src.sendMessage(Text.of("\u00A74Error: \u00A7cThis command's config is invalid! Please report to staff."));
                PixelUpgrade.log.info("\u00A74Weakness // critical: \u00A7cCheck your config. If need be, wipe and \\u00A74/pu reload\\u00A7c.");
            }
            else
            {
                printToLog(2, "Called by player \u00A73" + src.getName() + "\u00A7b. Starting!");

                Player player = (Player) src;
                boolean canContinue = true, commandConfirmed = false, inputIsInteger = false;
                String inputString = null;
                int inputInteger = 0;

                if (!args.<String>getOne("pokemon").isPresent())
                {
                    printToLog(2, "No arguments provided, aborting.");

                    checkAndAddHeader(commandCost, player);
                    src.sendMessage(Text.of("\u00A74Error: \u00A7cNo parameters found. Please provide a slot."));
                    printCorrectHelper(commandCost, player);
                    checkAndAddFooter(commandCost, player);

                    canContinue = false;
                }
                else
                {
                    inputString = args.<String>getOne("pokemon").get();

                    if (inputString.matches("^[0-9].*"))
                    {
                        inputIsInteger = true;
                        inputInteger = Integer.parseInt(args.<String>getOne("pokemon").get());

                        if (inputInteger > 802 || inputInteger < 1)
                        {
                            checkAndAddHeader(commandCost, player);
                            src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid Pok\u00E9dex number! Valid range is 1-802."));
                            src.sendMessage(Text.of("\u00A75Please note: \u00A7dYou can also enter a Pok\u00E9mon's name!"));
                            printCorrectHelper(commandCost, player);
                            checkAndAddFooter(commandCost, player);
                        }
                    }
                }

                if (args.hasAny("c"))
                    commandConfirmed = true;

                if (canContinue)
                {
                    printToLog(3, "No errors encountered yet, running code on input!");
                    EnumPokemonList returnedPokemon;
                    boolean isImmune = false, hasForms = false, hasAlolanVariants = false;

                    // Forms.
                    boolean providedCastform = false, providedWormadam = false, providedRotom = false, providedShaymin = false;
                    boolean providedDarmanitan = false, providedMeloetta = false, providedHoopa = false;

                    // Alolan variants. Oh boy.
                    boolean providedRattata = false, providedRaticate = false, providedRaichu = false, providedSandshrew = false;
                    boolean providedSandslash = false, providedVulpix = false, providedNinetales = false, providedDiglett = false;
                    boolean providedDugtrio = false, providedMeowth = false, providedPersian = false, providedGeodude = false;
                    boolean providedGraveler = false, providedGolem = false, providedGrimer = false, providedMuk = false;
                    boolean providedExeggutor = false, providedMarowak = false;

                    if (inputIsInteger)
                        returnedPokemon = getPokemonFromID(inputInteger);
                    else
                    {
                        if (getPokemonFromName(inputString) == null)
                        {
                            switch (inputString.toUpperCase())
                            {
                                /*                                                        *\
                                    TODO: Add space support for arguments. Low priority.
                                    Tapu Koko, Tapu Lele, Tapu Bunu, Tapu Fini = broken.
                                    Passing something like "tapukoko" should still work!
                                \*                                                        */

                                // Possibly dodgy inputs and names that are different internally for technical reasons.
                                case "NIDORANF": case "FNIDORAN": case "FEMALENIDORAN": case "NIDORAN♀":
                                    inputString = "NidoranFemale"; break;
                                case "NIDORANM": case "MNIDORAN": case "MALENIDORAN": case "NIDORAN♂":
                                    inputString = "NidoranMale"; break;
                                case "FARFETCH'D": case "FARFETCHED":
                                    inputString = "Farfetchd"; break;
                                case "MR. MIME": case "MR.MIME": case "MISTERMIME":
                                    inputString = "MrMime"; break;
                                case "MIME JR.": case "MIMEJR.": case "MIMEJUNIOR":
                                    inputString = "MimeJr"; break;
                                case "FLABÉBÉ": case "FLABÈBÈ":
                                    inputString = "Flabebe"; break;
                                case "TYPE:NULL": case "TYPE:": case "TYPE": // A bit cheeky, but nothing else starts with "type" right now.
                                    inputString = "TypeNull"; break;
                                case "JANGMO-O":
                                    inputString = "JangmoO"; break;
                                case "HAKAMO-O":
                                    inputString = "HakamoO"; break;
                                case "KOMMO-O":
                                    inputString = "KommoO"; break;
                            }
                        }
                        else
                        {
                            switch (inputString.toUpperCase())
                            {
                                // Forms. Used for helper message printing.
                                case "CASTFORM":
                                    providedCastform = true;
                                    hasForms = true; break;
                                case "WORMADAM":
                                    providedWormadam = true;
                                    hasForms = true; break;
                                case "ROTOM":
                                    providedRotom = true;
                                    hasForms = true; break;
                                case "SHAYMIN":
                                    providedShaymin = true;
                                    hasForms = true; break;
                                case "DARMANITAN":
                                    providedDarmanitan = true;
                                    hasForms = true; break;
                                case "MELOETTA":
                                    providedMeloetta = true;
                                    hasForms = true; break;
                                case "HOOPA":
                                    providedHoopa = true;
                                    hasForms = true; break;

                                // Alolan variants. Same as above.
                                case "RATTATA":
                                    providedRattata = true;
                                    hasAlolanVariants = true; break;
                                case "RATICATE":
                                    providedRaticate = true;
                                    hasAlolanVariants = true; break;
                                case "RAICHU":
                                    providedRaichu = true;
                                    hasAlolanVariants = true; break;
                                case "SANDSHREW":
                                    providedSandshrew = true;
                                    hasAlolanVariants = true; break;
                                case "SANDSLASH":
                                    providedSandslash = true;
                                    hasAlolanVariants = true; break;
                                case "VULPIX":
                                    providedVulpix = true;
                                    hasAlolanVariants = true; break;
                                case "NINETALES":
                                    providedNinetales = true;
                                    hasAlolanVariants = true; break;
                                case "DIGLETT":
                                    providedDiglett = true;
                                    hasAlolanVariants = true; break;
                                case "DUGTRIO":
                                    providedDugtrio = true;
                                    hasAlolanVariants = true; break;
                                case "MEOWTH":
                                    providedMeowth = true;
                                    hasAlolanVariants = true; break;
                                case "PERSIAN":
                                    providedPersian = true;
                                    hasAlolanVariants = true; break;
                                case "GEODUDE":
                                    providedGeodude = true;
                                    hasAlolanVariants = true; break;
                                case "GRAVELER":
                                    providedGraveler = true;
                                    hasAlolanVariants = true; break;
                                case "GOLEM":
                                    providedGolem = true;
                                    hasAlolanVariants = true; break;
                                case "GRIMER":
                                    providedGrimer = true;
                                    hasAlolanVariants = true; break;
                                case "MUK":
                                    providedMuk = true;
                                    hasAlolanVariants = true; break;
                                case "EXEGGUTOR":
                                    providedExeggutor = true;
                                    hasAlolanVariants = true; break;
                                case "MAROWAK":
                                    providedMarowak = true;
                                    hasAlolanVariants = true; break;
                            }
                        }

                        returnedPokemon = getPokemonFromName(inputString);
                    }

                    if (returnedPokemon == null)
                    {
                        checkAndAddHeader(commandCost, player);
                        src.sendMessage(Text.of("\u00A74Error: \u00A7cInvalid Pok\u00E9mon or Pok\u00E9dex number!"));
                        printCorrectHelper(commandCost, player);
                        checkAndAddFooter(commandCost, player);
                    }
                    else
                    {
                        //src.sendMessage(Text.of("\u00A74EFF: \u00A7c" + EnumType.getEffectiveness(EnumType.Fire, type1)));
                        // Attack type, defending type Water/Fire = Water attacking Fire, super effective, 2.0
                        boolean type2Present = true, hasAnyTypeAbilities = false, isShedinja = false;
                        boolean hasLevitate = false, hasLightningRod = false, hasMotorDrive = false, hasSapSipper = false;
                        boolean hasStormDrain = false, hasVoltAbsorb = false, hasWaterAbsorb = false, hasFlashFire = false;

                        int pNumber = returnedPokemon.index;
                        String pName = returnedPokemon.name();

                        String typeString =
                                "Normal, Fighting, Flying, Poison, Ground, Rock, " +
                                "Bug, Ghost, Steel, Fire, Water, Grass, " +
                                "Electric, Psychic, Ice, Dragon, Dark, Fairy";

                        String colorString = // Used to load in colors for the different types.
                                "\u00A7f, \u00A74, \u00A79, \u00A75, \u00A7e, \u00A77, " +
                                "\u00A72, \u00A75, \u00A77, \u00A7c, \u00A73, \u00A7a, " +
                                "\u00A7e, \u00A7d, \u00A7b, \u00A79, \u00A78, \u00A7d";

                        String levitateString = // Which Pokémon have Levitate, and are thusly immune to Ground?
                                "Gastly, Haunter, Gengar, Koffing, Weezing, Misdreavus, Unown, Vibrava, Flygon, " +
                                "Lunatone, Solrock, Baltoy, Claydol, Duskull, Chimecho, Latias, Latios, Mismagius, " +
                                "Chingling, Bronzor, Bronzong, Carnivine, Rotom, RotomHeat, RotomWash, RotomFrost, " +
                                "RotomFan, RotomMow, Uxie, Mesprit, Azelf, Giratina, Cresselia, Tynamo, Eelektrik, " +
                                "Eelektross, Cryogonal, Hydreigon, Vikavolt";

                        String lightningRodString = // Which Pokémon have Lightning Rod, and are thusly immune to Electric?
                                "Cubone, Marowak, Rhyhorn, Rhydon, Electrike, Manectric, Rhyperior, Blitzle, " +
                                "Zebstrika, Pikachu, Raichu, Goldeen, Seaking, Zapdos, Pichu, Plusle, Sceptile ";

                        String motorDriveString = // Which Pokémon have Motor Drive, and are thusly immune to Electric?
                                "Electivire, Blitzle, Zebstrika, Emolga";


                        String stormDrainString = // Which Pokémon have Storm Drain, and are thusly immune to Water?
                                "Lileep, Cradily, Shellos, Gastrodon, Finneon, Lumineon, Maractus";

                        String voltAbsorbString = // Which Pokémon have Volt Absorb, and are thusly immune to Electric?
                                "Jolteon, Chinchou, Lanturn, Thundurus, Raikou, Minun, Pachirisu";

                        String sapSipperString = // Which Pokémon have Sap Sipper, and are thusly immune to Grass?
                                "Deerling, Sawsbuck, Bouffalant, Skiddo, Gogoat, Goomy, Sliggoo, Goodra, Drampa, " +
                                        "Marill, Azumarill, Girafarig, Stantler, Miltank, Azurill, Blitzle, Zebstrika";

                        String waterAbsorbString = // Which Pokémon have Water Absorb, and are thusly immune to Water?
                                "Poliwag, Poliwhirl, Poliwrath, Lapras, Vaporeon, Politoed, Wooper, Quagsire, " +
                                "Mantine, Mantyke, Maractus, Frillish, Jellicent, Volcanion, Chinchou, Lanturn, " +
                                "Suicune, Cacnea, Cacturne, Tympole, Palpitoad, Seismitoad";

                        String flashFireString = // Which Pokémon have Flash Fire, and are thusly immune to Fire?
                                "Vulpix, Ninetales, Growlithe, Arcanine, Ponyta, Rapidash, Flareon, Houndour, " +
                                "Houndoom, Heatran, Litwick, Lampent, Chandelure, Heatmor, Cyndaquil, Quilava, " +
                                "Typhlosion, Entei";

                        String[] typeList = typeString.split(", ");
                        String[] colorList = colorString.split(", ");

                        if (Objects.equals(pName, "Shedinja"))
                            isShedinja = true;
                        if (levitateString.contains(pName))
                            hasLevitate = true;
                        if (lightningRodString.contains(pName))
                            hasLightningRod = true;
                        if (motorDriveString.contains(pName))
                            hasMotorDrive = true;
                        if (sapSipperString.contains(pName))
                            hasSapSipper = true;
                        if (stormDrainString.contains(pName))
                            hasStormDrain = true;
                        if (voltAbsorbString.contains(pName))
                            hasVoltAbsorb = true;
                        if (waterAbsorbString.contains(pName))
                            hasWaterAbsorb = true;
                        if (flashFireString.contains(pName))
                            hasFlashFire = true;
                        if (hasLevitate || hasLightningRod || hasMotorDrive || hasSapSipper)
                            hasAnyTypeAbilities = true;
                        if (hasStormDrain || hasVoltAbsorb || hasWaterAbsorb || hasFlashFire)
                            hasAnyTypeAbilities = true;

                        EnumType type1 = EnumType.parseType(returnedPokemon.type1);
                        EnumType type2 = EnumType.parseType(returnedPokemon.type2);
                        if (returnedPokemon.type2.contains("EMPTY"))
                            type2Present = false;

                        ArrayList<EnumType> foundTypes = new ArrayList<>();
                        foundTypes.add(type1);
                        if (type2Present)
                            foundTypes.add(type2);

                        StringBuilder weaknessBuilder2x = new StringBuilder(), weaknessBuilder4x = new StringBuilder();
                        StringBuilder strengthBuilder50p = new StringBuilder(), strengthBuilder25p = new StringBuilder();
                        StringBuilder immunityBuilder = new StringBuilder();

                        for (int i = 1; i < 19; i++)
                        {
                            EnumType typeToTest = EnumType.parseType(typeList[i - 1]);
                            float typeEffectiveness = getTotalEffectiveness(foundTypes, typeToTest);

                            if (typeEffectiveness < 1.0f)
                            {
                                if (typeEffectiveness == 0.5f)
                                {
                                    strengthBuilder50p.append(colorList[i - 1]); // Set up the type's color.
                                    strengthBuilder50p.append(typeList[i - 1]); // Add the actual type.
                                    strengthBuilder50p.append("\u00A7f, "); // Add a comma for the next type.
                                }
                                else if (typeEffectiveness == 0.25f)
                                {
                                    strengthBuilder25p.append(colorList[i - 1]); // Set up the type's color.
                                    strengthBuilder25p.append(typeList[i - 1]); // Add the actual type.
                                    strengthBuilder25p.append("\u00A7f, "); // Add a comma for the next type.
                                }
                                else if (typeEffectiveness == 0.00f)
                                {
                                    immunityBuilder.append(colorList[i - 1]); // Set up the type's color.
                                    immunityBuilder.append(typeList[i - 1]); // Add the actual type.
                                    immunityBuilder.append("\u00A7f, "); // Add a comma for the next type.
                                }

                                PixelUpgrade.log.info("\u00A72Debug, type is \u00A7a" + typeToTest + "\u00A72 and strength is \u00A7a" + typeEffectiveness);
                            }
                            else if (typeEffectiveness > 1.0f)
                            {
                                if (typeEffectiveness == 2.0f)
                                {
                                    weaknessBuilder2x.append(colorList[i - 1]); // Set up the type's color.
                                    weaknessBuilder2x.append(typeList[i - 1]); // Add the actual type.
                                    weaknessBuilder2x.append("\u00A7f, "); // Add a comma for the next type.
                                }
                                else if (typeEffectiveness == 4.0f)
                                {
                                    weaknessBuilder4x.append(colorList[i - 1]); // Set up the type's color.
                                    weaknessBuilder4x.append(typeList[i - 1]); // Add the actual type.
                                    weaknessBuilder4x.append("\u00A7f, "); // Add a comma for the next type.
                                }

                                PixelUpgrade.log.info("\u00A72Debug, type is \u00A7a" + typeToTest + "\u00A72 and weakness is \u00A7a" + typeEffectiveness);
                            }
                        }

                        if (weaknessBuilder2x.length() != 0 || weaknessBuilder4x.length() != 0)
                        {
                            player.sendMessage(Text.of("\u00A75(\u00A7d#" + pNumber + "\u00A75) \u00A76" + pName + "'s\u00A7c weaknesses\u00A76:"));
                            if (weaknessBuilder2x.length() != 0)
                            {
                                weaknessBuilder2x.setLength(weaknessBuilder2x.length() - 2); // Cut off the last comma.
                                player.sendMessage(Text.of("\u00A7c2x\u00A7f: " + weaknessBuilder2x));
                            }
                            if (weaknessBuilder4x.length() != 0)
                            {
                                weaknessBuilder4x.setLength(weaknessBuilder4x.length() - 2); // Cut off the last comma.
                                player.sendMessage(Text.of("\u00A7c4x\u00A7f: " + weaknessBuilder4x));
                            }
                        }

                        if (strengthBuilder50p.length() != 0 || strengthBuilder25p.length() != 0)
                        {
                            player.sendMessage(Text.of("\u00A75(\u00A7d#" + pNumber + "\u00A75) \u00A76" + pName + "'s\u00A7a resistances\u00A76:"));
                            if (strengthBuilder50p.length() != 0)
                            {
                                strengthBuilder50p.setLength(strengthBuilder50p.length() - 2); // Cut off the last comma.
                                player.sendMessage(Text.of("\u00A7a0.5x\u00A7f: " + strengthBuilder50p));
                            }
                            if (strengthBuilder25p.length() != 0)
                            {
                                strengthBuilder25p.setLength(strengthBuilder25p.length() - 2); // Cut off the last comma.
                                player.sendMessage(Text.of("\u00A7a0.25x\u00A7f: " + strengthBuilder25p));
                            }
                        }

                        if (immunityBuilder.length() != 0)
                        {
                            player.sendMessage(Text.of("\u00A75(\u00A7d#" + pNumber + "\u00A75) \u00A76" + pName + "'s\u00A7b immunities\u00A76:"));
                            immunityBuilder.setLength(immunityBuilder.length() - 2); // Cut off the last comma.
                            player.sendMessage(Text.of("\u00A7bImmune\u00A7f: " + immunityBuilder));
                        }

                        if (hasAnyTypeAbilities)
                        {
                            player.sendMessage(Text.of("\u00A75(\u00A7d#" + pNumber + "\u00A75) \u00A76" + pName + "'s\u00A7e type-affecting abilities\u00A76:"));

                            if (isShedinja)
                                player.sendMessage(Text.of("\u00A7e\u00A7nWonder Guard\u00A7r\u00A7f: \u00A77Only super-effective moves will hit."));
                            if (hasLevitate)
                                player.sendMessage(Text.of("\u00A7e\u00A7nLevitate\u00A7r\u00A7f: \u00A7eGround \u00A77moves may not do any damage."));
                            if (hasLightningRod)
                                player.sendMessage(Text.of("\u00A7e\u00A7nLightning Rod\u00A7r\u00A7f: \u00A7eElectric \u00A77moves may not do any damage."));
                            if (hasMotorDrive)
                                player.sendMessage(Text.of("\u00A7e\u00A7nMotor Drive\u00A7r\u00A7f: \u00A7eElectric \u00A77moves may not do any damage."));
                            if (hasSapSipper)
                                player.sendMessage(Text.of("\u00A7e\u00A7nSap Sipper\u00A7r\u00A7f: \u00A7aGrass \u00A77moves may not do any damage."));
                            if (hasStormDrain)
                                player.sendMessage(Text.of("\u00A7e\u00A7nStorm Drain\u00A7r\u00A7f: \u00A73Water \u00A77moves may not do any damage."));
                            if (hasVoltAbsorb)
                                player.sendMessage(Text.of("\u00A7e\u00A7nVolt Absorb\u00A7r\u00A7f: \u00A7eElectric \u00A77moves may not do any damage."));
                            if (hasWaterAbsorb)
                                player.sendMessage(Text.of("\u00A7e\u00A7nWater Absorb\u00A7r\u00A7f: \u00A73Water \u00A77moves may not do any damage."));
                            if (hasFlashFire)
                                player.sendMessage(Text.of("\u00A7e\u00A7nFlash Fire\u00A7r\u00A7f: \u00A7cFire \u00A77moves may not do any damage."));
                        }
                    }
                }
            }
        }
        else
            printToLog(0, "This command cannot run from the console or command blocks.");

        return CommandResult.success();
    }

    private void checkAndAddHeader(int cost, Player player)
    {
        if (cost > 0)
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
    }

    private void checkAndAddFooter(int cost, Player player)
    {
        if (cost > 0)
        {
            player.sendMessage(Text.of(""));
            player.sendMessage(Text.of("\u00A76Warning: \u00A7eAdd the -c flag only if you're sure!"));
            player.sendMessage(Text.of("\u00A7eConfirming will cost you \u00A76" + cost + "\u00A7e coins."));
            player.sendMessage(Text.of("\u00A75-----------------------------------------------------"));
        }
    }

    private void printCorrectHelper(int cost, Player player)
    {
        if (cost != 0)
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/weakness <Pok\u00E9mon name/number> {-c to confirm}"));
        else
            player.sendMessage(Text.of("\u00A74Usage: \u00A7c/weakness <Pok\u00E9mon name/number>"));
    }

    private void printToLog(int debugNum, String inputString)
    {
        if (debugNum <= debugLevel)
        {
            if (debugNum == 0)
                PixelUpgrade.log.info("\u00A74Weakness // critical: \u00A7c" + inputString);
            else if (debugNum == 1)
                PixelUpgrade.log.info("\u00A76Weakness // important: \u00A7e" + inputString);
            else if (debugNum == 2)
                PixelUpgrade.log.info("\u00A73Weakness // start/end: \u00A7b" + inputString);
            else
                PixelUpgrade.log.info("\u00A72Weakness // debug: \u00A7a" + inputString);
        }
    }


    public enum EnumPokemonList
    {
        /* Todo: Add Alolan alternatives. List below. Not relevant just yet, so eh.
            019 Rattata
            020 Raticate
            026 Raichu
            027 Sandshrew
            028 Sandslash
            037 Vulpix
            038 Ninetales
            050 Diglett
            051 Dugtrio
            052 Meowth
            053 Persian
            074 Geodude
            075 Graveler
            076 Golem
            088 Grimer
            089 Muk
            103 Exeggutor
            105 Marowak
        */

        // Gen 1
        Bulbasaur(1, "Grass, Poison"),
        Ivysaur(2, "Grass, Poison"),
        Venusaur(3, "Grass, Poison"),
        Charmander(4, "Fire"),
        Charmeleon(5, "Fire"),
        Charizard(6, "Fire, Flying"),
        Squirtle(7, "Water"),
        Wartortle(8, "Water"),
        Blastoise(9, "Water"),
        Caterpie(10, "Bug"),
        Metapod(11, "Bug"),
        Butterfree(12, "Bug, Flying"),
        Weedle(13, "Bug, Poison"),
        Kakuna(14, "Bug, Poison"),
        Beedrill(15, "Bug, Poison"),
        Pidgey(16, "Normal, Flying"),
        Pidgeotto(17, "Normal, Flying"),
        Pidgeot(18, "Normal, Flying"),
        Rattata(19, "Normal"),
        Raticate(20, "Normal"),
        Spearow(21, "Normal, Flying"),
        Fearow(22, "Normal, Flying"),
        Ekans(23, "Poison"),
        Arbok(24, "Poison"),
        Pikachu(25, "Electric"),
        Raichu(26, "Electric"),
        Sandshrew(27, "Ground"),
        Sandslash(28, "Ground"),
        NidoranFemale(29, "Poison"),
        Nidorina(30, "Poison"),
        Nidoqueen(31, "Poison, Ground"),
        NidoranMale(32, "Poison"),
        Nidorino(33, "Poison"),
        Nidoking(34, "Poison, Ground"),
        Clefairy(35, "Fairy"),
        Clefable(36, "Fairy"),
        Vulpix(37, "Fire"),
        Ninetales(38, "Fire"),
        Jigglypuff(39, "Normal, Fairy"),
        Wigglytuff(40, "Normal, Fairy"),
        Zubat(41, "Poison, Flying"),
        Golbat(42, "Poison, Flying"),
        Oddish(43, "Grass, Poison"),
        Gloom(44, "Grass, Poison"),
        Vileplume(45, "Grass, Poison"),
        Paras(46, "Bug, Grass"),
        Parasect(47, "Bug, Grass"),
        Venonat(48, "Bug, Poison"),
        Venomoth(49, "Bug, Poison"),
        Diglett(50, "Ground"),
        Dugtrio(51, "Ground"),
        Meowth(52, "Normal"),
        Persian(53, "Normal"),
        Psyduck(54, "Water"), // so tempted
        Golduck(55, "Water"),
        Mankey(56, "Fighting"),
        Primeape(57, "Fighting"),
        Growlithe(58, "Fire"),
        Arcanine(59, "Fire"),
        Poliwag(60, "Water"),
        Poliwhirl(61, "Water"),
        Poliwrath(62, "Water, Fighting"),
        Abra(63, "Psychic"),
        Kadabra(64, "Psychic"),
        Alakazam(65, "Psychic"),
        Machop(66, "Fighting"),
        Machoke(67, "Fighting"),
        Machamp(68, "Fighting"),
        Bellsprout(69, "Grass, Poison"),
        Weepinbell(70, "Grass, Poison"),
        Victreebel(71, "Grass, Poison"),
        Tentacool(72, "Water, Poison"),
        Tentacruel(73, "Water, Poison"),
        Geodude(74, "Rock, Ground"),
        Graveler(75, "Rock, Ground"),
        Golem(76, "Rock, Ground"),
        Ponyta(77, "Fire"),
        Rapidash(78, "Fire"),
        Slowpoke(79, "Water, Psychic"),
        Slowbro(80, "Water, Psychic"),
        Magnemite(81, "Electric, Steel"),
        Magneton(82, "Electric, Steel"),
        Farfetchd(83, "Normal, Flying"),
        Doduo(84, "Normal, Flying"),
        Dodrio(85, "Normal, Flying"),
        Seel(86, "Water"),
        Dewgong(87, "Water, Ice"),
        Grimer(88, "Poison"),
        Muk(89, "Poison"),
        Shellder(90, "Water"),
        Cloyster(91, "Water, Ice"),
        Gastly(92, "Ghost, Poison"),
        Haunter(93, "Ghost, Poison"),
        Gengar(94, "Ghost, Poison"),
        Onix(95, "Rock, Ground"),
        Drowzee(96, "Psychic"),
        Hypno(97, "Psychic"),
        Krabby(98, "Water"),
        Kingler(99, "Water"),
        Voltorb(100, "Electric"),
        Electrode(101, "Electric"),
        Exeggcute(102, "Grass, Psychic"),
        Exeggutor(103, "Grass, Psychic"),
        Cubone(104, "Ground"),
        Marowak(105, "Ground"),
        Hitmonlee(106, "Fighting"),
        Hitmonchan(107, "Fighting"),
        Lickitung(108, "Normal"),
        Koffing(109, "Poison"),
        Weezing(110, "Poison"),
        Rhyhorn(111, "Ground, Rock"),
        Rhydon(112, "Ground, Rock"),
        Chansey(113, "Normal"),
        Tangela(114, "Grass"),
        Kangaskhan(115, "Normal"),
        Horsea(116, "Water"),
        Seadra(117, "Water"),
        Goldeen(118, "Water"),
        Seaking(119, "Water"),
        Staryu(120, "Water"),
        Starmie(121, "Water, Psychic"),
        MrMime(122, "Psychic, Fairy"),
        Scyther(123, "Bug, Flying"),
        Jynx(124, "Ice, Psychic"),
        Electabuzz(125, "Electric"),
        Magmar(126, "Fire"),
        Pinsir(127, "Bug"),
        Tauros(128, "Normal"),
        Magikarp(129, "Water"),
        Gyarados(130, "Water, Flying"),
        Lapras(131, "Water, Ice"),
        Ditto(132, "Normal"),
        Eevee(133, "Normal"),
        Vaporeon(134, "Water"),
        Jolteon(135, "Electric"),
        Flareon(136, "Fire"),
        Porygon(137, "Normal"),
        Omanyte(138, "Rock, Water"),
        Omastar(139, "Rock, Water"),
        Kabuto(140, "Rock, Water"),
        Kabutops(141, "Rock, Water"),
        Aerodactyl(142, "Rock, Flying"),
        Snorlax(143, "Normal"),
        Articuno(144, "Ice, Flying"),
        Zapdos(145, "Electric, Flying"),
        Moltres(146, "Fire, Flying"),
        Dratini(147, "Dragon"),
        Dragonair(148, "Dragon"),
        Dragonite(149, "Dragon, Flying"),
        Mewtwo(150, "Psychic"),
        Mew(151, "Psychic"),

        // Gen 2 (also known as best gen)
        Chikorita(152, "Grass"),
        Bayleef(153, "Grass"),
        Meganium(154, "Grass"),
        Cyndaquil(155, "Fire"),
        Quilava(156, "Fire"),
        Typhlosion(157, "Fire"),
        Totodile(158, "Water"),
        Croconaw(159, "Water"),
        Feraligatr(160, "Water"),
        Sentret(161, "Normal"),
        Furret(162, "Normal"),
        Hoothoot(163, "Normal, Flying"),
        Noctowl(164, "Normal, Flying"),
        Ledyba(165, "Bug, Flying"),
        Ledian(166, "Bug, Flying"),
        Spinarak(167, "Bug, Poison"),
        Ariados(168, "Bug, Poison"),
        Crobat(169, "Poison, Flying"),
        Chinchou(170, "Water, Electric"),
        Lanturn(171, "Water, Electric"),
        Pichu(172, "Electric"),
        Cleffa(173, "Fairy"),
        Igglybuff(174, "Normal, Fairy"),
        Togepi(175, "Fairy"),
        Togetic(176, "Fairy, Flying"),
        Natu(177, "Psychic, Flying"),
        Xatu(178, "Psychic, Flying"),
        Mareep(179, "Electric"),
        Flaaffy(180, "Electric"),
        Ampharos(181, "Electric"),
        Bellossom(182, "Grass"),
        Marill(183, "Water, Fairy"),
        Azumarill(184, "Water, Fairy"),
        Sudowoodo(185, "Rock"),
        Politoed(186, "Water"),
        Hoppip(187, "Grass, Flying"),
        Skiploom(188, "Grass, Flying"),
        Jumpluff(189, "Grass, Flying"),
        Aipom(190, "Normal"),
        Sunkern(191, "Grass"),
        Sunflora(192, "Grass"),
        Yanma(193, "Bug, Flying"),
        Wooper(194, "Water, Ground"),
        Quagsire(195, "Water, Ground"),
        Espeon(196, "Psychic"),
        Umbreon(197, "Dark"),
        Murkrow(198, "Dark, Flying"),
        Slowking(199, "Water, Psychic"),
        Misdreavus(200, "Ghost"),
        Unown(201, "Psychic"),
        Wobbuffet(202, "Psychic"),
        Girafarig(203, "Normal, Psychic"),
        Pineco(204, "Bug"),
        Forretress(205, "Bug, Steel"),
        Dunsparce(206, "Normal"),
        Gligar(207, "Ground, Flying"),
        Steelix(208, "Steel, Ground"),
        Snubbull(209, "Fairy"),
        Granbull(210, "Fairy"),
        Qwilfish(211, "Water, Poison"),
        Scizor(212, "Bug, Steel"),
        Shuckle(213, "Bug, Rock"),
        Heracross(214, "Bug, Fighting"),
        Sneasel(215, "Dark, Ice"),
        Teddiursa(216, "Normal"),
        Ursaring(217, "Normal"),
        Slugma(218, "Fire"),
        Magcargo(219, "Fire, Rock"),
        Swinub(220, "Ice, Ground"),
        Piloswine(221, "Ice, Ground"),
        Corsola(222, "Water, Rock"),
        Remoraid(223, "Water"),
        Octillery(224, "Water"),
        Delibird(225, "Ice, Flying"),
        Mantine(226, "Water, Flying"),
        Skarmory(227, "Steel, Flying"),
        Houndour(228, "Dark, Fire"),
        Houndoom(229, "Dark, Fire"),
        Kingdra(230, "Water, Dragon"),
        Phanpy(231, "Ground"),
        Donphan(232, "Ground"),
        Porygon2(233, "Normal"),
        Stantler(234, "Normal"),
        Smeargle(235, "Normal"),
        Tyrogue(236, "Fighting"),
        Hitmontop(237, "Fighting"),
        Smoochum(238, "Ice, Psychic"),
        Elekid(239, "Electric"),
        Magby(240, "Fire"),
        Miltank(241, "Normal"),
        Blissey(242, "Normal"),
        Raikou(243, "Electric"),
        Entei(244, "Fire"),
        Suicune(245, "Water"),
        Larvitar(246, "Rock, Ground"),
        Pupitar(247, "Rock, Ground"),
        Tyranitar(248, "Rock, Dark"),
        Lugia(249, "Psychic, Flying"),
        HoOh(250, "Fire, Flying"),
        Celebi(251, "Psychic, Grass"),

        // Gen 3
        Treecko(252, "Grass"),
        Grovyle(253, "Grass"),
        Sceptile(254, "Grass"),
        Torchic(255, "Fire"),
        Combusken(256, "Fire, Fighting"),
        Blaziken(257, "Fire, Fighting"),
        Mudkip(258, "Water"),
        Marshtomp(259, "Water, Ground"),
        Swampert(260, "Water, Ground"),
        Poochyena(261, "Dark"),
        Mightyena(262, "Dark"),
        Zigzagoon(263, "Normal"),
        Linoone(264, "Normal"),
        Wurmple(265, "Bug"),
        Silcoon(266, "Bug"),
        Beautifly(267, "Bug, Flying"),
        Cascoon(268, "Bug"),
        Dustox(269, "Bug, Poison"),
        Lotad(270, "Water, Grass"),
        Lombre(271, "Water, Grass"),
        Ludicolo(272, "Water, Grass"),
        Seedot(273, "Grass"),
        Nuzleaf(274, "Grass, Dark"),
        Shiftry(275, "Grass, Dark"),
        Taillow(276, "Normal, Flying"),
        Swellow(277, "Normal, Flying"),
        Wingull(278, "Water, Flying"),
        Pelipper(279, "Water, Flying"),
        Ralts(280, "Psychic, Fairy"),
        Kirlia(281, "Psychic, Fairy"),
        Gardevoir(282, "Psychic, Fairy"),
        Surskit(283, "Bug, Water"),
        Masquerain(284, "Bug, Flying"),
        Shroomish(285, "Grass"),
        Breloom(286, "Grass, Fighting"),
        Slakoth(287, "Normal"),
        Vigoroth(288, "Normal"),
        Slaking(289, "Normal"),
        Nincada(290, "Bug, Ground"),
        Ninjask(291, "Bug, Flying"),
        Shedinja(292, "Bug, Ghost"),
        Whismur(293, "Normal"),
        Loudred(294, "Normal"),
        Exploud(295, "Normal"),
        Makuhita(296, "Fighting"),
        Hariyama(297, "Fighting"),
        Azurill(298, "Normal, Fairy"),
        Nosepass(299, "Rock"),
        Skitty(300, "Normal"),
        Delcatty(301, "Normal"),
        Sableye(302, "Dark, Ghost"),
        Mawile(303, "Steel, Fairy"),
        Aron(304, "Steel, Rock"),
        Lairon(305, "Steel, Rock"),
        Aggron(306, "Steel, Rock"),
        Meditite(307, "Fighting, Psychic"),
        Medicham(308, "Fighting, Psychic"),
        Electrike(309, "Electric"),
        Manectric(310, "Electric"),
        Plusle(311, "Electric"),
        Minun(312, "Electric"),
        Volbeat(313, "Bug"),
        Illumise(314, "Bug"),
        Roselia(315, "Grass, Poison"),
        Gulpin(316, "Poison"),
        Swalot(317, "Poison"),
        Carvanha(318, "Water, Dark"),
        Sharpedo(319, "Water, Dark"),
        Wailmer(320, "Water"),
        Wailord(321, "Water"),
        Numel(322, "Fire, Ground"),
        Camerupt(323, "Fire, Ground"),
        Torkoal(324, "Fire"),
        Spoink(325, "Psychic"),
        Grumpig(326, "Psychic"),
        Spinda(327, "Normal"),
        Trapinch(328, "Ground"),
        Vibrava(329, "Ground, Dragon"),
        Flygon(330, "Ground, Dragon"),
        Cacnea(331, "Grass"),
        Cacturne(332, "Grass, Dark"),
        Swablu(333, "Normal, Flying"),
        Altaria(334, "Dragon, Flying"),
        Zangoose(335, "Normal"),
        Seviper(336, "Poison"),
        Lunatone(337, "Rock, Psychic"),
        Solrock(338, "Rock, Psychic"), // Praise the sun!
        Barboach(339, "Water, Ground"),
        Whiscash(340, "Water, Ground"),
        Corphish(341, "Water"),
        Crawdaunt(342, "Water, Dark"),
        Baltoy(343, "Ground, Psychic"),
        Claydol(344, "Ground, Psychic"),
        Lileep(345, "Rock, Grass"),
        Cradily(346, "Rock, Grass"),
        Anorith(347, "Rock, Bug"),
        Armaldo(348, "Rock, Bug"),
        Feebas(349, "Water"),
        Milotic(350, "Water"),
        Castform(351, "Normal"),
        Kecleon(352, "Normal"),
        Shuppet(353, "Ghost"),
        Banette(354, "Ghost"),
        Duskull(355, "Ghost"),
        Dusclops(356, "Ghost"),
        Tropius(357, "Grass, Flying"),
        Chimecho(358, "Psychic"),
        Absol(359, "Dark"),
        Wynaut(360, "Psychic"), // Why?
        Snorunt(361, "Ice"),
        Glalie(362, "Ice"),
        Spheal(363, "Ice, Water"),
        Sealeo(364, "Ice, Water"),
        Walrein(365, "Ice, Water"),
        Clamperl(366, "Water"),
        Huntail(367, "Water"),
        Gorebyss(368, "Water"),
        Relicanth(369, "Water, Rock"),
        Luvdisc(370, "Water"),
        Bagon(371, "Dragon"),
        Shelgon(372, "Dragon"),
        Salamence(373, "Dragon, Flying"),
        Beldum(374, "Steel, Psychic"),
        Metang(375, "Steel, Psychic"),
        Metagross(376, "Steel, Psychic"),
        Regirock(377, "Rock"),
        Regice(378, "Ice"),
        Registeel(379, "Steel"),
        Latias(380, "Dragon, Psychic"),
        Latios(381, "Dragon, Psychic"),
        Kyogre(382, "Water"),
        Groudon(383, "Ground"),
        Rayquaza(384, "Dragon, Flying"),
        Jirachi(385, "Steel, Psychic"),
        Deoxys(386, "Psychic"),

        // Gen 4
        Turtwig(387, "Grass"),
        Grotle(388, "Grass"),
        Torterra(389, "Grass, Ground"),
        Chimchar(390, "Fire"),
        Monferno(391, "Fire, Fighting"),
        Infernape(392, "Fire, Fighting"),
        Piplup(393, "Water"),
        Prinplup(394, "Water"),
        Empoleon(395, "Water, Steel"),
        Starly(396, "Normal, Flying"),
        Staravia(397, "Normal, Flying"),
        Staraptor(398, "Normal, Flying"),
        Bidoof(399, "Normal"),
        Bibarel(400, "Normal, Water"),
        Kricketot(401, "Bug"),
        Kricketune(402, "Bug"),
        Shinx(403, "Electric"),
        Luxio(404, "Electric"),
        Luxray(405, "Electric"),
        Budew(406, "Grass, Poison"),
        Roserade(407, "Grass, Poison"),
        Cranidos(408, "Rock"),
        Rampardos(409, "Rock"),
        Shieldon(410, "Rock, Steel"),
        Bastiodon(411, "Rock, Steel"),
        Burmy(412, "Bug"),
        Wormadam(413, "Bug, Grass"),
        Mothim(414, "Bug, Flying"),
        Combee(415, "Bug, Flying"),
        Vespiquen(416, "Bug, Flying"),
        Pachirisu(417, "Electric"),
        Buizel(418, "Water"),
        Floatzel(419, "Water"),
        Cherubi(420, "Grass"),
        Cherrim(421, "Grass"),
        Shellos(422, "Water"),
        Gastrodon(423, "Water, Ground"),
        Ambipom(424, "Normal"),
        Drifloon(425, "Ghost, Flying"),
        Drifblim(426, "Ghost, Flying"),
        Buneary(427, "Normal"),
        Lopunny(428, "Normal"),
        Mismagius(429, "Ghost"),
        Honchkrow(430, "Dark, Flying"),
        Glameow(431, "Normal"),
        Purugly(432, "Normal"),
        Chingling(433, "Psychic"),
        Stunky(434, "Poison, Dark"),
        Skuntank(435, "Poison, Dark"),
        Bronzor(436, "Steel, Psychic"),
        Bronzong(437, "Steel, Psychic"),
        Bonsly(438, "Rock"),
        MimeJr(439, "Psychic, Fairy"),
        Happiny(440, "Normal"),
        Chatot(441, "Normal, Flying"),
        Spiritomb(442, "Ghost, Dark"),
        Gible(443, "Dragon, Ground"),
        Gabite(444, "Dragon, Ground"),
        Garchomp(445, "Dragon, Ground"),
        Munchlax(446, "Normal"),
        Riolu(447, "Fighting"),
        Lucario(448, "Fighting, Steel"),
        Hippopotas(449, "Ground"),
        Hippowdon(450, "Ground"),
        Skorupi(451, "Poison, Bug"),
        Drapion(452, "Poison, Dark"),
        Croagunk(453, "Poison, Fighting"),
        Toxicroak(454, "Poison, Fighting"),
        Carnivine(455, "Grass"),
        Finneon(456, "Water"),
        Lumineon(457, "Water"),
        Mantyke(458, "Water, Flying"),
        Snover(459, "Grass, Ice"),
        Abomasnow(460, "Grass, Ice"),
        Weavile(461, "Dark, Ice"),
        Magnezone(462, "Electric, Steel"),
        Lickilicky(463, "Normal"),
        Rhyperior(464, "Ground, Rock"),
        Tangrowth(465, "Grass"),
        Electivire(466, "Electric"),
        Magmortar(467, "Fire"),
        Togekiss(468, "Fairy, Flying"),
        Yanmega(469, "Bug, Flying"),
        Leafeon(470, "Grass"),
        Glaceon(471, "Ice"),
        Gliscor(472, "Ground, Flying"),
        Mamoswine(473, "Ice, Ground"),
        PorygonZ(474, "Normal"),
        Gallade(475, "Psychic, Fighting"),
        Probopass(476, "Rock, Steel"),
        Dusknoir(477, "Ghost"),
        Froslass(478, "Ice, Ghost"),
        Rotom(479, "Electric, Ghost"),
        Uxie(480, "Psychic"),
        Mesprit(481, "Psychic"),
        Azelf(482, "Psychic"),
        Dialga(483, "Steel, Dragon"),
        Palkia(484, "Water, Dragon"),
        Heatran(485, "Fire, Steel"),
        Regigigas(486, "Normal"),
        Giratina(487, "Ghost, Dragon"),
        Cresselia(488, "Psychic"),
        Phione(489, "Water"),
        Manaphy(490, "Water"),
        Darkrai(491, "Dark"),
        Shaymin(492, "Grass"),
        Arceus(493, "Normal"),

        // Gen 5
        Victini(494, "Psychic, Fire"),
        Snivy(495, "Grass"),
        Servine(496, "Grass"),
        Serperior(497, "Grass"),
        Tepig(498, "Fire"),
        Pignite(499, "Fire, Fighting"),
        Emboar(500, "Fire, Fighting"),
        Oshawott(501, "Water"),
        Dewott(502, "Water"),
        Samurott(503, "Water"),
        Patrat(504, "Normal"),
        Watchog(505, "Normal"),
        Lillipup(506, "Normal"),
        Herdier(507, "Normal"),
        Stoutland(508, "Normal"),
        Purrloin(509, "Dark"),
        Liepard(510, "Dark"),
        Pansage(511, "Grass"),
        Simisage(512, "Grass"),
        Pansear(513, "Fire"),
        Simisear(514, "Fire"),
        Panpour(515, "Water"),
        Simipour(516, "Water"),
        Munna(517, "Psychic"),
        Musharna(518, "Psychic"),
        Pidove(519, "Normal, Flying"),
        Tranquill(520, "Normal, Flying"),
        Unfezant(521, "Normal, Flying"),
        Blitzle(522, "Electric"),
        Zebstrika(523, "Electric"),
        Roggenrola(524, "Rock"),
        Boldore(525, "Rock"),
        Gigalith(526, "Rock"),
        Woobat(527, "Psychic, Flying"),
        Swoobat(528, "Psychic, Flying"),
        Drilbur(529, "Ground"),
        Excadrill(530, "Ground, Steel"),
        Audino(531, "Normal"),
        Timburr(532, "Fighting"),
        Gurdurr(533, "Fighting"),
        Conkeldurr(534, "Fighting"),
        Tympole(535, "Water"),
        Palpitoad(536, "Water, Ground"),
        Seismitoad(537, "Water, Ground"),
        Throh(538, "Fighting"),
        Sawk(539, "Fighting"),
        Sewaddle(540, "Bug, Grass"),
        Swadloon(541, "Bug, Grass"),
        Leavanny(542, "Bug, Grass"),
        Venipede(543, "Bug, Poison"),
        Whirlipede(544, "Bug, Poison"),
        Scolipede(545, "Bug, Poison"),
        Cottonee(546, "Grass, Fairy"),
        Whimsicott(547, "Grass, Fairy"),
        Petilil(548, "Grass"),
        Lilligant(549, "Grass"),
        Basculin(550, "Water"),
        Sandile(551, "Ground, Dark"),
        Krokorok(552, "Ground, Dark"),
        Krookodile(553, "Ground, Dark"),
        Darumaka(554, "Fire"),
        Darmanitan(555, "Fire"),
        Maractus(556, "Grass"),
        Dwebble(557, "Bug, Rock"),
        Crustle(558, "Bug, Rock"),
        Scraggy(559, "Dark, Fighting"),
        Scrafty(560, "Dark, Fighting"),
        Sigilyph(561, "Psychic, Flying"),
        Yamask(562, "Ghost"),
        Cofagrigus(563, "Ghost"),
        Tirtouga(564, "Water, Rock"),
        Carracosta(565, "Water, Rock"),
        Archen(566, "Rock, Flying"),
        Archeops(567, "Rock, Flying"),
        Trubbish(568, "Poison"),
        Garbodor(569, "Poison"),
        Zorua(570, "Dark"),
        Zoroark(571, "Dark"),
        Minccino(572, "Normal"),
        Cinccino(573, "Normal"),
        Gothita(574, "Psychic"),
        Gothorita(575, "Psychic"),
        Gothitelle(576, "Psychic"),
        Solosis(577, "Psychic"),
        Duosion(578, "Psychic"),
        Reuniclus(579, "Psychic"),
        Ducklett(580, "Water, Flying"),
        Swanna(581, "Water, Flying"),
        Vanillite(582, "Ice"),
        Vanillish(583, "Ice"),
        Vanilluxe(584, "Ice"),
        Deerling(585, "Normal, Grass"),
        Sawsbuck(586, "Normal, Grass"),
        Emolga(587, "Electric, Flying"),
        Karrablast(588, "Bug"),
        Escavalier(589, "Bug, Steel"),
        Foongus(590, "Grass, Poison"),
        Amoonguss(591, "Grass, Poison"),
        Frillish(592, "Water, Ghost"),
        Jellicent(593, "Water, Ghost"),
        Alomomola(594, "Water"),
        Joltik(595, "Bug, Electric"),
        Galvantula(596, "Bug, Electric"),
        Ferroseed(597, "Grass, Steel"),
        Ferrothorn(598, "Grass, Steel"),
        Klink(599, "Steel"),
        Klang(600, "Steel"),
        Klinklang(601, "Steel"),
        Tynamo(602, "Electric"),
        Eelektrik(603, "Electric"),
        Eelektross(604, "Electric"),
        Elgyem(605, "Psychic"),
        Beheeyem(606, "Psychic"),
        Litwick(607, "Ghost, Fire"),
        Lampent(608, "Ghost, Fire"),
        Chandelure(609, "Ghost, Fire"),
        Axew(610, "Dragon"),
        Fraxure(611, "Dragon"),
        Haxorus(612, "Dragon"),
        Cubchoo(613, "Ice"),
        Beartic(614, "Ice"),
        Cryogonal(615, "Ice"),
        Shelmet(616, "Bug"),
        Accelgor(617, "Bug"),
        Stunfisk(618, "Ground, Electric"),
        Mienfoo(619, "Fighting"),
        Mienshao(620, "Fighting"),
        Druddigon(621, "Dragon"),
        Golett(622, "Ground, Ghost"),
        Golurk(623, "Ground, Ghost"),
        Pawniard(624, "Dark, Steel"),
        Bisharp(625, "Dark, Steel"),
        Bouffalant(626, "Normal"),
        Rufflet(627, "Normal, Flying"),
        Braviary(628, "Normal, Flying"),
        Vullaby(629, "Dark, Flying"),
        Mandibuzz(630, "Dark, Flying"),
        Heatmor(631, "Fire"),
        Durant(632, "Bug, Steel"),
        Deino(633, "Dark, Dragon"),
        Zweilous(634, "Dark, Dragon"),
        Hydreigon(635, "Dark, Dragon"),
        Larvesta(636, "Bug, Fire"),
        Volcarona(637, "Bug, Fire"),
        Cobalion(638, "Steel, Fighting"),
        Terrakion(639, "Rock, Fighting"),
        Virizion(640, "Grass, Fighting"),
        Tornadus(641, "Flying"),
        Thundurus(642, "Electric, Flying"),
        Reshiram(643, "Dragon, Fire"),
        Zekrom(644, "Dragon, Electric"),
        Landorus(645, "Ground, Flying"),
        Kyurem(646, "Dragon, Ice"),
        Keldeo(647, "Water, Fighting"),
        Meloetta(648, "Normal, Psychic"),
        Genesect(649, "Bug, Steel"),

        // Gen 6
        Chespin(650, "Grass"),
        Quilladin(651, "Grass"),
        Chesnaught(652, "Grass, Fighting"),
        Fennekin(653, "Fire"),
        Braixen(654, "Fire"),
        Delphox(655, "Fire, Psychic"),
        Froakie(656, "Water"),
        Frogadier(657, "Water"),
        Greninja(658, "Water, Dark"),
        Bunnelby(659, "Normal"),
        Diggersby(660, "Normal, Ground"),
        Fletchling(661, "Normal, Flying"),
        Fletchinder(662, "Fire, Flying"),
        Talonflame(663, "Fire, Flying"),
        Scatterbug(664, "Bug"),
        Spewpa(665, "Bug"),
        Vivillon(666, "Bug, Flying"),
        Litleo(667, "Fire, Normal"),
        Pyroar(668, "Fire, Normal"),
        Flabebe(669, "Fairy"),
        Floette(670, "Fairy"),
        Florges(671, "Fairy"),
        Skiddo(672, "Grass"),
        Gogoat(673, "Grass"),
        Pancham(674, "Fighting"),
        Pangoro(675, "Fighting, Dark"),
        Furfrou(676, "Normal"),
        Espurr(677, "Psychic"),
        Meowstic(678, "Psychic"),
        Honedge(679, "Steel, Ghost"),
        Doublade(680, "Steel, Ghost"),
        Aegislash(681, "Steel, Ghost"),
        Spritzee(682, "Fairy"),
        Aromatisse(683, "Fairy"),
        Swirlix(684, "Fairy"),
        Slurpuff(685, "Fairy"),
        Inkay(686, "Dark, Psychic"),
        Malamar(687, "Dark, Psychic"),
        Binacle(688, "Rock, Water"),
        Barbaracle(689, "Rock, Water"),
        Skrelp(690, "Poison, Water"),
        Dragalge(691, "Poison, Dragon"),
        Clauncher(692, "Water"),
        Clawitzer(693, "Water"),
        Helioptile(694, "Electric, Normal"),
        Heliolisk(695, "Electric, Normal"),
        Tyrunt(696, "Rock, Dragon"),
        Tyrantrum(697, "Rock, Dragon"),
        Amaura(698, "Rock, Ice"),
        Aurorus(699, "Rock, Ice"),
        Sylveon(700, "Fairy"),
        Hawlucha(701, "Fighting, Flying"),
        Dedenne(702, "Electric, Fairy"),
        Carbink(703, "Rock, Fairy"),
        Goomy(704, "Dragon"),
        Sliggoo(705, "Dragon"),
        Goodra(706, "Dragon"),
        Klefki(707, "Steel, Fairy"),
        Phantump(708, "Ghost, Grass"),
        Trevenant(709, "Ghost, Grass"),
        Pumpkaboo(710, "Ghost, Grass"),
        Gourgeist(711, "Ghost, Grass"),
        Bergmite(712, "Ice"),
        Avalugg(713, "Ice"),
        Noibat(714, "Flying, Dragon"),
        Noivern(715, "Flying, Dragon"),
        Xerneas(716, "Fairy"),
        Yveltal(717, "Dark, Flying"),
        Zygarde(718, "Dragon, Ground"),
        Diancie(719, "Rock, Fairy"),
        Hoopa(720, "Psychic, Ghost"),
        Volcanion(721, "Fire, Water"),

        // Gen 7
        Rowlet(722, "Grass, Flying"),
        Dartrix(723, "Grass, Flying"),
        Decidueye(724, "Grass, Ghost"),
        Litten(725, "Fire"),
        Torracat(726, "Fire"),
        Incineroar(727, "Fire, Dark"),
        Popplio(728, "Water"),
        Brionne(729, "Water"),
        Primarina(730, "Water, Fairy"),
        Pikipek(731, "Normal, Flying"),
        Trumbeak(732, "Normal, Flying"),
        Toucannon(733, "Normal, Flying"),
        Yungoos(734, "Normal"),
        Gumshoos(735, "Normal"),
        Grubbin(736, "Bug"),
        Charjabug(737, "Bug, Electric"),
        Vikavolt(738, "Bug, Electric"),
        Crabrawler(739, "Fighting"),
        Crabominable(740, "Fighting, Ice"),
        Oricorio(741, "Fire, Flying"),
        Cutiefly(742, "Bug, Fairy"),
        Ribombee(743, "Bug, Fairy"),
        Rockruff(744, "Rock"),
        Lycanroc(745, "Rock"),
        Wishiwashi(746, "Water"),
        Mareanie(747, "Poison, Water"),
        Toxapex(748, "Poison, Water"),
        Mudbray(749, "Ground"),
        Mudsdale(750, "Ground"),
        Dewpider(751, "Water, Bug"),
        Araquanid(752, "Water, Bug"),
        Fomantis(753, "Grass"),
        Lurantis(754, "Grass"),
        Morelull(755, "Grass, Fairy"),
        Shiinotic(756, "Grass, Fairy"),
        Salandit(757, "Poison, Fire"),
        Salazzle(758, "Poison, Fire"),
        Stufful(759, "Normal, Fighting"),
        Bewear(760, "Normal, Fighting"),
        Bounsweet(761, "Grass"),
        Steenee(762, "Grass"),
        Tsareena(763, "Grass"),
        Comfey(764, "Fairy"),
        Oranguru(765, "Normal, Psychic"),
        Passimian(766, "Fighting"),
        Wimpod(767, "Bug, Water"),
        Golisopod(768, "Bug, Water"),
        Sandygast(769, "Ghost, Ground"),
        Palossand(770, "Ghost, Ground"),
        Pyukumuku(771, "Water"),
        TypeNull(772, "Normal"),
        Silvally(773, "Normal"),
        Minior(774, "Rock, Flying"),
        Komala(775, "Normal"),
        Turtonator(776, "Fire, Dragon"),
        Togedemaru(777, "Electric, Steel"),
        Mimikyu(778, "Ghost, Fairy"),
        Bruxish(779, "Water, Psychic"),
        Drampa(780, "Normal, Dragon"),
        Dhelmise(781, "Ghost, Grass"),
        JangmoO(782, "Dragon"),
        HakamoO(783, "Dragon, Fighting"),
        KommoO(784, "Dragon, Fighting"),
        TapuKoko(785, "Electric, Fairy"),
        TapuLele(786, "Psychic, Fairy"),
        TapuBulu(787, "Grass, Fairy"),
        TapuFini(788, "Water, Fairy"),
        Cosmog(789, "Psychic"),
        Cosmoem(790, "Psychic"),
        Solgaleo(791, "Psychic, Steel"),
        Lunala(792, "Psychic, Ghost"),
        Nihilego(793, "Rock, Poison"),
        Buzzwole(794, "Bug, Fighting"),
        Pheromosa(795, "Bug, Fighting"),
        Xurkitree(796, "Electric"),
        Celesteela(797, "Steel, Flying"),
        Kartana(798, "Grass, Steel"),
        Guzzlord(799, "Dark, Dragon"),
        Necrozma(800, "Psychic"),
        Magearna(801, "Steel, Fairy"),
        Marshadow(802, "Fighting, Ghost"),

        // Forms. Be careful -- cannot be accessed or checked through ID, as it disallows checking 0.
        CastformSunny(0, "Fire"),
        CastformRainy(0, "Water"),
        CastformSnowy(0, "Ice"),
        WormadamSandy(0, "Bug, Ground"),
        WormadamTrash(0, "Bug, Steel"),
        RotomHeat(0, "Electric, Fire"),
        RotomWash(0, "Electric, Water"),
        RotomFrost(0, "Electric, Ice"),
        RotomFan(0, "Electric, Flying"),
        RotomMow(0, "Electric, Grass"),
        ShayminSky(0, "Grass, Flying"),
        DarmanitanZen(0, "Fire, Psychic"),
        MeloettaPirouette(0, "Normal, Fighting"),
        HoopaUnbound(0, "Psychic, Dark"),

        // Alolan Pokémon variants. Same rules as above.
        RattataAlolan(0, "Dark, Normal"),
        RaticateAlolan(0, "Dark, Normal"),
        RaichuAlolan(0, "Electric, Psychic"),
        SandshrewAlolan(0, "Ice, Steel"),
        SandslashAlolan(0, "Ice, Steel"),
        VulpixAlolan(0, "Ice"),
        NinetalesAlolan(0, "Ice, Fairy"),
        DiglettAlolan(0, "Ground, Steel"),
        DugtrioAlolan(0, "Ground, Steel"),
        MeowthAlolan(0, "Dark"),
        PersianAlolan(0, "Dark"),
        GeodudeAlolan(0, "Rock, Electric"),
        GravelerAlolan(0, "Rock, Electric"),
        GolemAlolan(0, "Rock, Electric"),
        GrimerAlolan(0, "Poison, Dark"),
        MukAlolan(0, "Poison, Dark"),
        ExeggutorAlolan(0, "Grass, Dragon"),
        MarowakAlolan(0, "Fire, Ghost");

        // Set up some variables for the Pokémon check.
        public int index;
        public String type1, type2;

        EnumPokemonList(int index, String types)
        {
            this.index = index;
            String[] delimitedTypes = types.split(", ");
            int typeCount = delimitedTypes.length;

            if (typeCount == 2)
            {
                type1 = delimitedTypes[0];
                type2 = delimitedTypes[1];
            }
            else
            {
                type1 = delimitedTypes[0];
                type2 = "EMPTY";
            }
        }

        public static EnumPokemonList getPokemonFromID(int index)
        {
            EnumPokemonList[] values = values();
            EnumPokemonList pokemon = values[index - 1];

            if (pokemon != null)
                return values[index - 1];
            else
                return null;
        }

        public static EnumPokemonList getPokemonFromName(String name)
        {
            EnumPokemonList[] allValues = values();

            for (EnumPokemonList pokemon : allValues)
            {
                if (pokemon.name().equalsIgnoreCase(name))
                    return pokemon;
            }
            // If the loop does not find and return a Pokémon, do this.
            return null;
        }
    }

    /*
    //-------------------------------------------------------//
    // Testing routine for new additions to EnumPokemonList. //
    // Uncomment this if you need to test further additions. //
    //-------------------------------------------------------//

    // Taken from http://www.java2s.com/Tutorials/Java/Data_Type_How_to/String/Check_if_enum_contains_a_given_string.htm
    private static <E extends Enum<E>> boolean contains(Class<E> _enumClass,
                                                       String value) {
        try {
            return EnumSet.allOf(_enumClass)
                    .contains(Enum.valueOf(_enumClass, value));
        } catch (Exception e) {
            return false;
        }
    }

    enum validTypeList
    {
        Normal,
        Fighting,
        Flying,
        Poison,
        Ground,
        Rock,
        Bug,
        Ghost,
        Steel,
        Fire,
        Water,
        Grass,
        Electric,
        Psychic,
        Ice,
        Dragon,
        Dark,
        Fairy,
        EMPTY
    }

    //---------------------------------------------------//
    // Add the following under the inputIsInteger check. //
    // Make sure to comment out code it complains about. //
    //---------------------------------------------------//

    for (int i = 1; i < 803; i++) // UPDATE ME (replace 803 with your last Pokémon's ID, +1)
    {
        returnedPokemon = getPokemonFromID(i);

        if (!contains(validTypeList.class, returnedPokemon.type1) || !contains(validTypeList.class, returnedPokemon.type2))
            PixelUpgrade.log.info("\u00A74Array error found! \u00A7c" + returnedPokemon.index + " | " + returnedPokemon.type1 + " | " + returnedPokemon.type2);
    }
    */
}