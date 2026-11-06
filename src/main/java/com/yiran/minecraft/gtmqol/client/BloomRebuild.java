package com.yiran.minecraft.gtmqol.client;

import net.minecraft.core.SectionPos;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;

/** The bloom buffer of one section rebuild, see {@code BloomSafeModeMixin}. */
public record BloomRebuild(SectionPos section, ByteBufferBuilder bytes, BufferBuilder builder) {}
