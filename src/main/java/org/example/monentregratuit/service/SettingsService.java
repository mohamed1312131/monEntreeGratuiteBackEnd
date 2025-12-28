package org.example.monentregratuit.service;

import lombok.AllArgsConstructor;
import org.example.monentregratuit.entity.AboutUs;
import org.example.monentregratuit.entity.AboutUsQA;
import org.example.monentregratuit.entity.SocialLinks;
import org.example.monentregratuit.entity.Video;
import org.example.monentregratuit.repo.AboutUsRepository;
import org.example.monentregratuit.repo.AboutUsQARepository;
import org.example.monentregratuit.repo.SocialLinksRepository;
import org.example.monentregratuit.repo.VideoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SettingsService {

    private final AboutUsRepository aboutUsRepository;
    private final AboutUsQARepository aboutUsQARepository;
    private final VideoRepository videoRepository;
    private final SocialLinksRepository socialLinksRepository;

    // About Us methods
    public List<AboutUs> getAllAboutUs() {
        return aboutUsRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<AboutUs> getActiveAboutUs() {
        return aboutUsRepository.findByIsActiveTrue();
    }

    public AboutUs getAboutUsById(Long id) {
        return aboutUsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("About Us section not found with ID: " + id));
    }

    public AboutUs createAboutUs(AboutUs aboutUsDetails) {
        AboutUs aboutUs = new AboutUs();
        aboutUs.setTitle(aboutUsDetails.getTitle());
        aboutUs.setDescription(aboutUsDetails.getDescription());
        aboutUs.setImageUrl(aboutUsDetails.getImageUrl());
        aboutUs.setVideoUrl(aboutUsDetails.getVideoUrl());
        aboutUs.setIsActive(aboutUsDetails.getIsActive());
        aboutUs.setEventsCount(aboutUsDetails.getEventsCount());
        aboutUs.setVisitorsCount(aboutUsDetails.getVisitorsCount());
        aboutUs.setExhibitorsCount(aboutUsDetails.getExhibitorsCount());
        
        if (aboutUsDetails.getQaList() != null && !aboutUsDetails.getQaList().isEmpty()) {
            for (AboutUsQA qaDetails : aboutUsDetails.getQaList()) {
                AboutUsQA qa = new AboutUsQA();
                qa.setAboutUs(aboutUs);
                qa.setQuestion(qaDetails.getQuestion());
                qa.setResponse(qaDetails.getResponse());
                qa.setDisplayOrder(qaDetails.getDisplayOrder());
                aboutUs.getQaList().add(qa);
            }
        }
        
        return aboutUsRepository.save(aboutUs);
    }

    public AboutUs updateAboutUs(Long id, AboutUs aboutUsDetails) {
        AboutUs aboutUs = getAboutUsById(id);
        aboutUs.setTitle(aboutUsDetails.getTitle());
        aboutUs.setDescription(aboutUsDetails.getDescription());
        aboutUs.setImageUrl(aboutUsDetails.getImageUrl());
        aboutUs.setVideoUrl(aboutUsDetails.getVideoUrl());
        aboutUs.setIsActive(aboutUsDetails.getIsActive());
        
        aboutUs.getQaList().clear();
        if (aboutUsDetails.getQaList() != null && !aboutUsDetails.getQaList().isEmpty()) {
            for (AboutUsQA qaDetails : aboutUsDetails.getQaList()) {
                AboutUsQA qa = new AboutUsQA();
                qa.setAboutUs(aboutUs);
                qa.setQuestion(qaDetails.getQuestion());
                qa.setResponse(qaDetails.getResponse());
                qa.setDisplayOrder(qaDetails.getDisplayOrder());
                aboutUs.getQaList().add(qa);
            }
        }
        
        return aboutUsRepository.save(aboutUs);
    }

    public void deleteAboutUs(Long id) {
        aboutUsRepository.deleteById(id);
    }

    public AboutUs toggleAboutUsActive(Long id) {
        AboutUs aboutUs = getAboutUsById(id);
        aboutUs.setIsActive(!aboutUs.getIsActive());
        return aboutUsRepository.save(aboutUs);
    }

    // Video methods
    public List<Video> getAllVideos() {
        return videoRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Video> getActiveVideos() {
        return videoRepository.findByIsActiveTrue();
    }

    public Video getVideoById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with ID: " + id));
    }

    public Video createVideo(Video video) {
        return videoRepository.save(video);
    }

    public Video updateVideo(Long id, Video videoDetails) {
        Video video = getVideoById(id);
        video.setName(videoDetails.getName());
        video.setLink(videoDetails.getLink());
        video.setDescription(videoDetails.getDescription());
        video.setIsActive(videoDetails.getIsActive());
        return videoRepository.save(video);
    }

    public void deleteVideo(Long id) {
        videoRepository.deleteById(id);
    }

    public Video toggleVideoActive(Long id) {
        Video video = getVideoById(id);
        video.setIsActive(!video.getIsActive());
        return videoRepository.save(video);
    }

    // Social Links methods
    public SocialLinks getSocialLinks() {
        List<SocialLinks> links = socialLinksRepository.findAll();
        if (links.isEmpty()) {
            // Create default social links if none exist
            SocialLinks defaultLinks = new SocialLinks();
            defaultLinks.setFacebook("");
            defaultLinks.setInstagram("");
            defaultLinks.setLinkedin("");
            defaultLinks.setTwitter("");
            defaultLinks.setYoutube("");
            return socialLinksRepository.save(defaultLinks);
        }
        return links.get(0);
    }

    public SocialLinks updateSocialLinks(SocialLinks socialLinksDetails) {
        SocialLinks socialLinks = getSocialLinks();
        socialLinks.setFacebook(socialLinksDetails.getFacebook());
        socialLinks.setInstagram(socialLinksDetails.getInstagram());
        socialLinks.setLinkedin(socialLinksDetails.getLinkedin());
        socialLinks.setTwitter(socialLinksDetails.getTwitter());
        socialLinks.setYoutube(socialLinksDetails.getYoutube());
        return socialLinksRepository.save(socialLinks);
    }

    public List<AboutUsQA> getQAByAboutUsId(Long aboutUsId) {
        return aboutUsQARepository.findByAboutUsIdOrderByDisplayOrderAsc(aboutUsId);
    }

    public AboutUsQA createQA(Long aboutUsId, AboutUsQA qa) {
        AboutUs aboutUs = getAboutUsById(aboutUsId);
        qa.setAboutUs(aboutUs);
        return aboutUsQARepository.save(qa);
    }

    public AboutUsQA updateQA(Long qaId, AboutUsQA qaDetails) {
        AboutUsQA qa = aboutUsQARepository.findById(qaId)
                .orElseThrow(() -> new RuntimeException("Q&A not found with ID: " + qaId));
        qa.setQuestion(qaDetails.getQuestion());
        qa.setResponse(qaDetails.getResponse());
        qa.setDisplayOrder(qaDetails.getDisplayOrder());
        return aboutUsQARepository.save(qa);
    }

    public void deleteQA(Long qaId) {
        aboutUsQARepository.deleteById(qaId);
    }
}
