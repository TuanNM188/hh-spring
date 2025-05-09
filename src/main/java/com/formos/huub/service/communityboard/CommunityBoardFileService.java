package com.formos.huub.service.communityboard;

import com.formos.huub.domain.entity.CommunityBoardFile;
import com.formos.huub.domain.enums.*;
import com.formos.huub.domain.response.communityboard.ResponseCommunityBoardFile;
import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.FileUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.mapper.communityboard.CommunityBoardFileMapper;
import com.formos.huub.repository.CommunityBoardFileRepository;
import com.formos.huub.repository.CommunityBoardGroupMemberRepository;
import com.formos.huub.repository.CommunityBoardGroupSettingRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.service.file.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static com.formos.huub.security.SecurityUtils.getCurrentUser;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityBoardFileService {

    FileService fileService;
    CommunityBoardFileRepository fileRepository;
    CommunityBoardFileMapper fileMapper;
    CommunityBoardGroupMemberRepository groupMemberRepository;
    CommunityBoardGroupSettingRepository groupSettingRepository;
    UserRepository userRepository;

    public String uploadFileCommunityBoard(MultipartFile multipartFile, String fileType) {

        validateFile(fileType, multipartFile.getSize(), multipartFile.getOriginalFilename());
        return fileService.uploadFile(multipartFile, "community-board");
    }

    public Map<String, Object> findFilesInGroup(UUID groupId, String type, Pageable pageable) {
        var fileType = CommunityBoardFileTypeEnum.FILE.getValue().equals(type)
            ? CommunityBoardFileTypeEnum.FILE
            : CommunityBoardFileTypeEnum.IMAGE;
        var currentUser = getCurrentUser(userRepository);
        var files = fileRepository.findAllByEntryTypeAndEntryIdAndType(CommunityBoardEntryTypeEnum.GROUP, currentUser.getId(), groupId, fileType, pageable);
        return PageUtils.toPage(files);
    }

    public ResponseCommunityBoardFile uploadFile(UUID groupId, MultipartFile multipartFile, String fileType) {
        var mediaType = Enum.valueOf(CommunityBoardFileTypeEnum.class, fileType);
        var currentUser = getCurrentUser(userRepository);
        validateMemberUploadFileInGroup(currentUser.getId(), groupId, mediaType);
        var url = uploadFileCommunityBoard(multipartFile, fileType);
        var file = CommunityBoardFile
            .builder()
            .entryType(CommunityBoardEntryTypeEnum.GROUP)
            .entryId(groupId)
            .type(mediaType)
            .ownerId(currentUser.getId())
            .name(multipartFile.getOriginalFilename())
            .realName(multipartFile.getOriginalFilename())
            .path(url)
            .build();

        fileRepository.save(file);
        return fileMapper.toResponse(file);
    }

    public void deleteFile(UUID fileId) {
        var file = fileRepository.findById(fileId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0010, "Community Board File")));

        if (!isAllowDeleteFile(file)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
        }
        fileRepository.delete(file);
    }

    private boolean isAllowDeleteFile(CommunityBoardFile file) {
        var currentUser = getCurrentUser(userRepository);
        if (currentUser.getId().equals(file.getOwnerId())) {
            return true;
        }
        var groupRole = Arrays.asList(CommunityBoardGroupRoleEnum.ORGANIZER, CommunityBoardGroupRoleEnum.MODERATOR);
        if (CommunityBoardEntryTypeEnum.GROUP.equals(file.getEntryType())) {
            return groupMemberRepository.findByGroupIdAndUserId(file.getEntryId(), currentUser.getId())
                .map(groupMember -> groupRole.contains(groupMember.getGroupRole()))
                .orElse(false);
        }
        return false;
    }

    private boolean isOrganizer(CommunityBoardGroupRoleEnum groupRole) {
        return CommunityBoardGroupRoleEnum.ORGANIZER.equals(groupRole);
    }

    private boolean isModeratorWithValidSetting(CommunityBoardGroupRoleEnum groupRole, String settingGroup) {
        return CommunityBoardGroupRoleEnum.MODERATOR.equals(groupRole)
            && !CommunityBoardSettingValueEnum.ORGANIZERS_ONLY.getValue().equals(settingGroup);
    }

    private boolean isMemberWithValidSetting(CommunityBoardGroupRoleEnum groupRole, String settingGroup) {
        return CommunityBoardSettingValueEnum.ALL_GROUP_MEMBERS.getValue().equals(settingGroup)
            && CommunityBoardGroupRoleEnum.MEMBER.equals(groupRole);
    }

    private void validateMemberUploadFileInGroup(UUID userId, UUID groupId, CommunityBoardFileTypeEnum type) {
        var groupMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057)));
        if (isOrganizer(groupMember.getGroupRole())) {
            return;
        }

        SettingKeyCodeEnum settingKey = null;
        switch (type) {
            case FILE -> settingKey = SettingKeyCodeEnum.GROUP_DOCUMENTS;
            case IMAGE -> settingKey = SettingKeyCodeEnum.GROUP_PHOTOS;
            case VIDEO -> settingKey = SettingKeyCodeEnum.GROUP_VIDEOS;
        }
        var settingGroup = groupSettingRepository.findByGroupIdAndSettingKey(groupId, settingKey)
            .orElseThrow(() -> new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "Group")));

        if (isModeratorWithValidSetting(groupMember.getGroupRole(), settingGroup) ||
            isMemberWithValidSetting(groupMember.getGroupRole(), settingGroup)) {
            return;
        }
        throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0057));
    }

    private void validateFile(String fileType, long size, String fileName) {
        String extension = FileUtils.getExtensionName(fileName);
        String actualFileType = FileUtils.getFileType(extension);

        validateFileSize(fileType, size);
        validateFileType(fileType, actualFileType);
    }

    private void validateFileSize(String fileType, long size) {
        long maxSize = CommunityBoardFileTypeEnum.VIDEO.getValue().equals(fileType)
            ? AppConstants.MAX_SIZE_VIDEO_IN_POST
            : AppConstants.MAX_SIZE_FILE_IN_POST;

        if (FileUtils.checkSize(maxSize, size)) {
            var errorKey = CommunityBoardFileTypeEnum.VIDEO.getValue().equals(fileType)
                ? Message.Keys.E0060
                : Message.Keys.E0061;
            throw new BadRequestException(MessageHelper.getMessage(errorKey));
        }
    }

    private void validateFileType(String expectedFileType, String actualFileType) {
        boolean isValidType = switch (actualFileType) {
            case FileUtils.VIDEO -> CommunityBoardFileTypeEnum.VIDEO.getValue().equals(expectedFileType);
            case FileUtils.IMAGE -> CommunityBoardFileTypeEnum.IMAGE.getValue().equals(expectedFileType);
            case FileUtils.TXT -> CommunityBoardFileTypeEnum.FILE.getValue().equals(expectedFileType);
            default -> false;
        };

        if (!isValidType) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0020, "File Type"));
        }
    }

}
