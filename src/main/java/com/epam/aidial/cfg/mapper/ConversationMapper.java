package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.ConversationDto;
import com.epam.aidial.cfg.dto.ConversationNodeInfoDto;
import com.epam.aidial.cfg.dto.ConversationsEximDto;
import com.epam.aidial.cfg.model.Conversation;
import com.epam.aidial.cfg.model.ConversationNodeInfo;
import com.epam.aidial.cfg.model.ConversationsExim;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    ConversationNodeInfoDto toConversationNodeInfoDto(ConversationNodeInfo model);

    ConversationsEximDto toConversationsEximDto(ConversationsExim model);

    ConversationDto toConversationDto(Conversation model);
}