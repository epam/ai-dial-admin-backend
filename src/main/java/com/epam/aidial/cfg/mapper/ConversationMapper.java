package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.ConversationEximDto;
import com.epam.aidial.cfg.dto.ConversationNodeInfoDto;
import com.epam.aidial.cfg.dto.ConversationsEximDto;
import com.epam.aidial.cfg.model.ConversationExim;
import com.epam.aidial.cfg.model.ConversationNodeInfo;
import com.epam.aidial.cfg.model.ConversationsExim;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    ConversationNodeInfoDto toConversationNodeInfoDto(ConversationNodeInfo model);

    ConversationsEximDto toConversationsEximDto(ConversationsExim model);

    ConversationEximDto toConversationEximDto(ConversationExim model);
}