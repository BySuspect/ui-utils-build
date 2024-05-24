package org.uiutils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.uiutils.MainClient;
import org.uiutils.SharedVariables;

import java.util.Objects;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Inject(at = @At("HEAD"), method = "onResourcePackSend", cancellable = true)
    public void onResourcePackSend(ResourcePackSendS2CPacket packet, CallbackInfo ci) {
        if (SharedVariables.bypassResourcePack && (packet.isRequired() || SharedVariables.resourcePackForceDeny)) {

            ResourcePackStatusC2SPacket.Status status = SharedVariables.resourcePackForceDeny ? ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD : ResourcePackStatusC2SPacket.Status.ACCEPTED;

            this.sendPacket(new ResourcePackStatusC2SPacket(ResourcePackStatusC2SPacket.Status.ACCEPTED));
            this.sendPacket(new ResourcePackStatusC2SPacket(ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
            MainClient.LOGGER.info(
                    "[UI Utils]: Required Resource Pack Bypassed, Message: " +
                            (Objects.requireNonNull(packet.getPrompt()).getString().isEmpty() ? "<no message>" : Objects.requireNonNull(packet.getPrompt()).getString()) +
                            ", URL: " + (packet.getURL() == null ? "<no url>" : packet.getURL())
            );
            ci.cancel();
        }
    }
}
