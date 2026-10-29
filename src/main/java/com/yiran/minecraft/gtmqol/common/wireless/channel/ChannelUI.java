package com.yiran.minecraft.gtmqol.common.wireless.channel;

import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanel;
import com.yiran.minecraft.gtmqol.GTMQoLAddon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.DynamicDrawable;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.MouseData;
import brachy.modularui.value.IntValue;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.GenericListSyncHandler;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static com.yiran.minecraft.gtmqol.common.ui.PopupWidgets.popup;

/**
 * Channel settings of one endpoint, after Flux Networks' network tabs. The machine's main area shows the bound
 * channel and opens the channel list ({@link ChannelListWidget}: click a channel to bind, its edit button to edit;
 * New and Unbind below). New and edit open a small form. Every change runs on the server as the player who opened
 * the UI and reports back in a feedback line.
 */
public final class ChannelUI {

    private static final String PREFIX = "gtmqol.channel.";
    public static final String CHANNEL_KEY = PREFIX + "current";
    public static final String NOT_BOUND_KEY = PREFIX + "status.not_bound";
    public static final String ACTIVE_KEY = PREFIX + "status.active";
    public static final String INACTIVE_KEY = PREFIX + "status.inactive";
    public static final String CHANNELS_KEY = PREFIX + "channels";
    public static final String UNBIND_KEY = PREFIX + "unbind";
    public static final String NEW_KEY = PREFIX + "new";
    public static final String CREATE_TITLE_KEY = PREFIX + "form.create";
    public static final String EDIT_TITLE_KEY = PREFIX + "form.edit";
    public static final String NAME_KEY = PREFIX + "form.name";
    public static final String SAVE_KEY = PREFIX + "form.save";
    public static final String DELETE_KEY = PREFIX + "form.delete";
    public static final String DELETE_TOOLTIP_KEY = PREFIX + "form.delete.tooltip";
    public static final String LIST_EMPTY_KEY = PREFIX + "list.empty";
    public static final String ROW_OWNER_KEY = PREFIX + "list.owner";
    public static final String ROW_COUNTS_KEY = PREFIX + "list.counts";
    public static final String ROW_INACTIVE_KEY = PREFIX + "list.inactive";
    public static final String ROW_BIND_HINT_KEY = PREFIX + "list.hint.bind";
    public static final String ROW_BOUND_HINT_KEY = PREFIX + "list.hint.bound";
    public static final String ROW_EDIT_HINT_KEY = PREFIX + "list.hint.edit";
    public static final String BOUND_KEY = PREFIX + "message.bound";
    public static final String UNBOUND_KEY = PREFIX + "message.unbound";
    public static final String CREATED_KEY = PREFIX + "message.created";
    public static final String SAVED_KEY = PREFIX + "message.saved";
    public static final String DELETED_KEY = PREFIX + "message.deleted";

    private static final int LIST_WIDTH = 196;
    private static final int LIST_HEIGHT = 132;
    /** Preset colors, like Flux Networks' network colors. */
    private static final int[] COLORS = { 0x2BC4F5, 0x3FBF5F, 0xF5D43B, 0xF58A2B, 0xE84343, 0xC452E8, 0x8C8C8C,
            0xFFFFFF };

    private static boolean langAdded;

    private ChannelUI() {}

    /** {@code player} is the one who opened the UI; on the server every action runs as them. */
    public static Flow create(PanelSyncManager syncManager, ChannelBindingTrait binding, Player player) {
        boolean client = player.level().isClientSide();
        String[] feedback = { "" };
        // form actions: the error of the last one (empty when it worked), and a count of those that worked,
        // which tells the client to close the form
        String[] formError = { "" };
        int[] formDone = { 0 };

        var currentName = new StringSyncValue(() -> {
            Channel channel = binding.getChannel();
            return channel == null ? "" : channel.getName();
        });
        var currentColor = new IntSyncValue(() -> {
            Channel channel = binding.getChannel();
            return channel == null ? 0 : channel.getColor();
        });
        var status = new StringSyncValue(() -> {
            Channel channel = binding.getChannel();
            return channel == null ? NOT_BOUND_KEY : channel.isActive() ? ACTIVE_KEY : INACTIVE_KEY;
        });
        var feedbackValue = new StringSyncValue(() -> feedback[0]);
        var formErrorValue = new StringSyncValue(() -> formError[0]);
        var formDoneValue = new IntSyncValue(() -> formDone[0]);
        var channels = new GenericListSyncHandler.Builder<ChannelInfo>()
                .getter(() -> client ? List.of() : ChannelSavedData.get().visible(player, binding.kind).stream()
                        .map(channel -> ChannelInfo.of(channel, player)).toList())
                .serializer(ChannelInfo::write)
                .deserializer(ChannelInfo::read)
                .build();
        syncManager.syncValue("channel_name", currentName);
        syncManager.syncValue("channel_color", currentColor);
        syncManager.syncValue("channel_status", status);
        syncManager.syncValue("channel_feedback", feedbackValue);
        syncManager.syncValue("channel_list", channels);
        syncManager.syncValue("channel_form_error", formErrorValue);
        syncManager.syncValue("channel_form_done", formDoneValue);

        syncManager.registerSyncedAction("channel_bind", false, true,
                buf -> feedback[0] = orElse(binding.bind(player, buf.readVarInt()), BOUND_KEY));
        syncManager.registerSyncedAction("channel_unbind", false, true, buf -> {
            binding.unbind();
            feedback[0] = UNBOUND_KEY;
        });
        // a form action's error stays in the form; when it works the form closes and the list says so
        Consumer<FormResult> formAction = result -> {
            formError[0] = result.error == null ? "" : result.error;
            if (result.error == null) {
                formDone[0]++;
                feedback[0] = result.success;
            }
        };
        syncManager.registerSyncedAction("channel_create", false, true, buf -> {
            Form form = Form.read(buf);
            formAction.accept(new FormResult(ChannelSavedData.get().create(player, binding.kind, form.name,
                    form.color, form.access), CREATED_KEY));
        });
        syncManager.registerSyncedAction("channel_edit", false, true, buf -> {
            int id = buf.readVarInt();
            Form form = Form.read(buf);
            formAction.accept(new FormResult(ChannelSavedData.get().edit(player, id, form.name, form.color,
                    form.access), SAVED_KEY));
        });
        syncManager.registerSyncedAction("channel_delete", false, true, buf -> formAction.accept(
                new FormResult(ChannelSavedData.get().delete(player, buf.readVarInt()), DELETED_KEY)));

        // the form's fields live on the client until sent
        var formState = new FormState();
        IPanelHandler form = syncManager.syncedPanel("channel_form", false,
                (manager, handler) -> formPanel(syncManager, formState, formErrorValue));
        formDoneValue.setChangeListener(() -> {
            if (!formState.waiting) return;
            formState.waiting = false;
            if (form.isPanelOpen()) form.closePanel();
        });
        IPanelHandler list = syncManager.syncedPanel("channel_list_panel", false,
                (manager, handler) -> listPanel(syncManager, binding, channels, formState, form, feedbackValue));

        return Flow.col()
                .width(MachineUIPanel.DEFAULT_CONTENT_WIDTH)
                .height(MachineUIPanel.DEFAULT_CONTENT_HEIGHT)
                .mainAxisAlignment(Alignment.MainAxis.CENTER)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .childPadding(5)
                .child(new TextWidget<>(Text.dynamic(() -> currentName.getValue().isEmpty() ?
                        Component.translatable(NOT_BOUND_KEY) :
                        Component.translatable(CHANNEL_KEY, Component.literal(currentName.getValue())
                                .withStyle(style -> style.withColor(currentColor.getIntValue()))))))
                .child(new TextWidget<>(Text.dynamic(() -> {
                    String key = status.getValue();
                    if (key.isEmpty() || key.equals(NOT_BOUND_KEY)) return Component.empty();
                    return Component.translatable(key)
                            .withStyle(key.equals(ACTIVE_KEY) ? ChatFormatting.DARK_GREEN : ChatFormatting.RED);
                })).maxWidth(MachineUIPanel.DEFAULT_CONTENT_WIDTH - 8))
                .child(new ButtonWidget<>()
                        .size(80, 18)
                        .overlay(Text.lang(CHANNELS_KEY))
                        .onMousePressed((context, button) -> {
                            list.togglePanel();
                            return true;
                        }))
                .child(feedbackLine(feedbackValue, MachineUIPanel.DEFAULT_CONTENT_WIDTH - 8));
    }

    private static String orElse(@Nullable String error, String success) {
        return error != null ? error : success;
    }

    private static TextWidget<?> feedbackLine(StringSyncValue feedback, int width) {
        return new TextWidget<>(Text.dynamic(() -> feedback.getValue().isEmpty() ? Component.empty() :
                Component.translatable(feedback.getValue()).withStyle(ChatFormatting.DARK_GRAY)))
                .maxWidth(width);
    }

    private static ModularPanel<?> listPanel(PanelSyncManager syncManager, ChannelBindingTrait binding,
                                             GenericListSyncHandler<ChannelInfo> channels,
                                             FormState formState, IPanelHandler form, StringSyncValue feedback) {
        var rows = new ChannelListWidget(channels::getValue, binding::getChannelId,
                channel -> syncManager.callSyncedAction("channel_bind", buf -> buf.writeVarInt(channel.id())),
                channel -> {
                    formState.edit(channel);
                    if (!form.isPanelOpen()) form.openPanel();
                },
                LIST_WIDTH - 8);
        return popup("channel_list_panel", LIST_WIDTH + 12, LIST_HEIGHT + 70)
                .child(Flow.col()
                        .padding(6)
                        .childPadding(5)
                        .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                        .child(Text.lang(binding.kind.langKey()).asWidget())
                        .child(new ListWidget<>()
                                .size(LIST_WIDTH, LIST_HEIGHT)
                                .child(rows))
                        .child(Flow.row()
                                .coverChildren()
                                .childPadding(6)
                                .child(new ButtonWidget<>()
                                        .size(60, 16)
                                        .overlay(Text.lang(NEW_KEY))
                                        .onMousePressed((context, button) -> {
                                            formState.create();
                                            if (!form.isPanelOpen()) form.openPanel();
                                            return true;
                                        }))
                                .child(new ButtonWidget<>()
                                        .size(60, 16)
                                        .overlay(Text.lang(UNBIND_KEY))
                                        .onMousePressed((context, button) -> {
                                            syncManager.callSyncedAction("channel_unbind");
                                            return true;
                                        })))
                        .child(feedbackLine(feedback, LIST_WIDTH)));
    }

    /** Closes itself once the server reports the action worked (the {@code channel_form_done} listener). */
    private static ModularPanel<?> formPanel(PanelSyncManager syncManager, FormState state,
                                             StringSyncValue error) {
        Flow colors = Flow.row().coverChildren().childPadding(3);
        for (int color : COLORS) {
            colors.child(new ButtonWidget<>()
                    .size(12)
                    .background(new Rectangle().color(0xFF000000 | color))
                    .overlay(new DynamicDrawable(() -> state.color.getIntValue() == color ?
                            new Rectangle().color(0x80000000).cornerRadius(1) : IDrawable.EMPTY).asIcon().size(6))
                    .onMousePressed((context, button) -> {
                        state.color.setIntValue(color);
                        return true;
                    }));
        }
        return popup("channel_form", 168, 124)
                .child(Flow.col()
                        .padding(6)
                        .childPadding(5)
                        .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                        .child(new TextWidget<>(Text.dynamic(() -> Component.translatable(
                                state.editing == 0 ? CREATE_TITLE_KEY : EDIT_TITLE_KEY))))
                        .child(Flow.row()
                                .coverChildren()
                                .childPadding(4)
                                .child(Text.lang(NAME_KEY).asWidget().height(14))
                                .child(new TextFieldWidget()
                                        .size(110, 14)
                                        .setMaxLength(Channel.MAX_NAME_LENGTH)
                                        // else the name only reaches the value on unfocus, after Save sent it
                                        .autoUpdateOnChange(true)
                                        .value(state.name)))
                        .child(colors)
                        .child(new ButtonWidget<>()
                                .size(150, 14)
                                .overlay(Text.dynamic(() -> Component.translatable(state.access.langKey())))
                                .onMousePressed((context, button) -> {
                                    state.access = state.access.next();
                                    return true;
                                }))
                        .child(Flow.row()
                                .coverChildren()
                                .childPadding(6)
                                // Delete is hidden while creating; Save then stays centered
                                .collapseDisabledChildren()
                                .child(new ButtonWidget<>()
                                        .size(60, 16)
                                        .overlay(Text.lang(SAVE_KEY))
                                        .onMousePressed((context, button) -> {
                                            state.waiting = true;
                                            if (state.editing == 0) {
                                                syncManager.callSyncedAction("channel_create", state::write);
                                            } else {
                                                int id = state.editing;
                                                syncManager.callSyncedAction("channel_edit", buf -> {
                                                    buf.writeVarInt(id);
                                                    state.write(buf);
                                                });
                                            }
                                            return true;
                                        }))
                                .child(new ButtonWidget<>()
                                        .size(60, 16)
                                        .overlay(Text.lang(DELETE_KEY))
                                        .setEnabledIf(button -> state.editing != 0)
                                        .tooltip(new RichTooltip().addLine(Component.translatable(DELETE_TOOLTIP_KEY)))
                                        .onMousePressed((context, button) -> {
                                            if (!MouseData.create(button).shift()) return false;
                                            state.waiting = true;
                                            int id = state.editing;
                                            syncManager.callSyncedAction("channel_delete", buf -> buf.writeVarInt(id));
                                            return true;
                                        })))
                        // only the answer to this form's own action
                        .child(new TextWidget<>(Text.dynamic(() -> !state.waiting || error.getValue().isEmpty() ?
                                Component.empty() :
                                Component.translatable(error.getValue()).withStyle(ChatFormatting.RED)))
                                .maxWidth(156)));
    }

    /** A form action's outcome on the server: {@code error} a lang key, or null with {@code success} to report. */
    private record FormResult(@Nullable String error, String success) {}

    /** What the form sends; read on the server. */
    private record Form(String name, int color, ChannelAccess access) {

        static Form read(FriendlyByteBuf buf) {
            return new Form(buf.readUtf(Channel.MAX_NAME_LENGTH * 4), buf.readInt(),
                    ChannelAccess.byId(buf.readVarInt()));
        }
    }

    /**
     * The form's fields on the client. {@code editing} is the channel id being edited, 0 when creating;
     * {@code waiting}: an action was sent and its answer hasn't closed the form.
     */
    private static final class FormState {

        final StringValue name = new StringValue("");
        final IntValue color = new IntValue(COLORS[0]);
        ChannelAccess access = ChannelAccess.PRIVATE;
        int editing;
        boolean waiting;

        void create() {
            editing = 0;
            waiting = false;
            name.setStringValue("");
            color.setIntValue(COLORS[0]);
            access = ChannelAccess.PRIVATE;
        }

        void edit(ChannelInfo channel) {
            editing = channel.id();
            waiting = false;
            name.setStringValue(channel.name());
            color.setIntValue(channel.color());
            access = channel.access();
        }

        void write(FriendlyByteBuf buf) {
            buf.writeUtf(name.getStringValue(), Channel.MAX_NAME_LENGTH * 4);
            buf.writeInt(color.getIntValue());
            buf.writeVarInt(access.ordinal());
        }
    }

    /** Lang of the channel UI and messages. Called by each kind's machines; adds it once. */
    public static void addLang() {
        // the lang provider throws on a duplicate key
        if (langAdded) return;
        langAdded = true;
        addLang(ChannelKind.COMPUTATION.langKey(), "Computation Channels");
        addLang(ChannelKind.OPTICAL.langKey(), "Optical Data Channels");
        addLang(ChannelAccess.PRIVATE.langKey(), "Private: only you");
        addLang(ChannelAccess.TEAM.langKey(), "Team: your FTB team");
        addLang(ChannelAccess.PUBLIC.langKey(), "Public: everyone");
        addLang(ChannelAccess.PRIVATE.shortLangKey(), "Private");
        addLang(ChannelAccess.TEAM.shortLangKey(), "Team");
        addLang(ChannelAccess.PUBLIC.shortLangKey(), "Public");
        addLang(CHANNEL_KEY, "Channel: %s");
        addLang(NOT_BOUND_KEY, "Not bound to a channel");
        addLang(ACTIVE_KEY, "Connected");
        addLang(INACTIVE_KEY, "Not working: more than one transmitter or receiver needs a router");
        addLang(CHANNELS_KEY, "Channels");
        addLang(UNBIND_KEY, "Unbind");
        addLang(NEW_KEY, "New");
        addLang(CREATE_TITLE_KEY, "New Channel");
        addLang(EDIT_TITLE_KEY, "Edit Channel");
        addLang(NAME_KEY, "Name");
        addLang(SAVE_KEY, "Save");
        addLang(DELETE_KEY, "Delete");
        addLang(DELETE_TOOLTIP_KEY, "Shift-click to delete; everything bound to it is unbound");
        addLang(LIST_EMPTY_KEY, "No channels yet: create one with New");
        addLang(ROW_OWNER_KEY, "Owner: %s");
        addLang(ROW_COUNTS_KEY, "Transmitters: %s, receivers: %s, routers: %s");
        addLang(ROW_INACTIVE_KEY, "Not working: needs a router for more than one transmitter or receiver");
        addLang(ROW_BIND_HINT_KEY, "Click to bind");
        addLang(ROW_BOUND_HINT_KEY, "Bound");
        addLang(ROW_EDIT_HINT_KEY, "Click to edit");
        addLang(BOUND_KEY, "Bound");
        addLang(UNBOUND_KEY, "Unbound");
        addLang(CREATED_KEY, "Channel created");
        addLang(SAVED_KEY, "Channel saved");
        addLang(DELETED_KEY, "Channel deleted");
        addLang(ChannelSavedData.NOT_FOUND_KEY, "That channel no longer exists");
        addLang(ChannelSavedData.NOT_ALLOWED_KEY, "Not allowed");
        addLang(ChannelSavedData.BAD_NAME_KEY, "Name must be 1 to " + Channel.MAX_NAME_LENGTH + " characters");
        addLang(ChannelSavedData.ROUTER_NEEDED_KEY, "This channel already has one; bind a router to it for more");
    }

    private static void addLang(String key, String value) {
        GTMQoLAddon.registrate().addRawLang(key, value);
    }
}
