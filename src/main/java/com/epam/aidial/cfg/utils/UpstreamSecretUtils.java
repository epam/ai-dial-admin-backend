package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.core.config.CoreUpstream;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Objects;

/**
 * An upstream holds its secrets on two levels: {@code key}/{@code secretExtraData} of its own, and a
 * pair of namesakes inside every entry of its {@code interfaces} map. Both levels must be handled
 * together everywhere secrets are stripped from an export or split off into the secured config.
 */
@UtilityClass
public final class UpstreamSecretUtils {

    /**
     * Nulls out every secret of the given upstreams, per-interface ones included. Only the values of
     * the {@code interfaces} map are mutated, never the map itself, so the immutable defaults are safe.
     */
    public static void removeSecrets(List<Upstream> upstreams) {
        if (CollectionUtils.isEmpty(upstreams)) {
            return;
        }
        for (Upstream upstream : upstreams) {
            upstream.setKey(null);
            upstream.setSecretExtraData(null);
            MapUtils.emptyIfNull(upstream.getInterfaces())
                    .values()
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(upstreamInterface -> {
                        upstreamInterface.setKey(null);
                        upstreamInterface.setSecretExtraData(null);
                    });
        }
    }

    /**
     * Whether the upstream holds a secret at its own level or inside any of its interfaces.
     */
    public static boolean hasSecrets(CoreUpstream upstream) {
        if (upstream == null) {
            return false;
        }
        if (upstream.getKey() != null || upstream.getSecretExtraData() != null) {
            return true;
        }
        return MapUtils.emptyIfNull(upstream.getInterfaces())
                .values()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(upstreamInterface -> upstreamInterface.getKey() != null
                        || upstreamInterface.getSecretExtraData() != null);
    }
}
