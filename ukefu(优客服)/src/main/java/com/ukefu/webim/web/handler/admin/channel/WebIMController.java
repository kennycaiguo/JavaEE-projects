package com.ukefu.webim.web.handler.admin.channel;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.ukefu.util.Menu;
import com.ukefu.webim.service.cache.CacheHelper;
import com.ukefu.webim.service.repository.ConsultInviteRepository;
import com.ukefu.webim.service.repository.OrganRepository;
import com.ukefu.webim.service.repository.SNSAccountRepository;
import com.ukefu.webim.service.repository.UserRepository;
import com.ukefu.webim.web.handler.Handler;
import com.ukefu.webim.web.model.CousultInvite;

@Controller
@RequestMapping("/admin/webim")
public class WebIMController extends Handler{
	
	@Autowired
	private ConsultInviteRepository invite;
	
	@Autowired
	private OrganRepository organRes ;
	
	@Autowired
	private UserRepository userRes ;
	
	@Value("${web.upload-path}")
    private String path;
	
	@Autowired
	private SNSAccountRepository snsAccountRes;

    @RequestMapping("/index")
    @Menu(type = "webim" , subtype = "webim" , admin= true)
    public ModelAndView index(ModelMap map , HttpServletRequest request , @Valid String snsid) {
    	
    	CousultInvite coultInvite = invite.findBySnsaccountidAndOrgi(snsid, super.getOrgi(request)) ;
    	if(coultInvite!=null){
    		map.addAttribute("inviteData", coultInvite);
    	}
    	map.addAttribute("skillList", organRes.findByOrgiAndSkill(super.getOrgi(request), true)) ;
    	map.addAttribute("agentList", userRes.findByOrgiAndAgent(super.getOrgi(request), true , new PageRequest(0, super.getPs(request)))) ;
    	
    	map.addAttribute("import", request.getServerPort()) ;
    	
    	map.addAttribute("snsAccount", snsAccountRes.findBySnsidAndOrgi(snsid, super.getOrgi(request))) ;
    	
    	
        return request(super.createAdminTempletResponse("/admin/webim/index"));
    }
    
    @RequestMapping("/save")
    @Menu(type = "admin" , subtype = "webim" , admin= true)
    public ModelAndView save(HttpServletRequest request , @Valid CousultInvite inviteData , @RequestParam(value = "webimlogo", required = false) MultipartFile webimlogo,@RequestParam(value = "agentheadimg", required = false) MultipartFile agentheadimg) throws IOException {
    	if(!StringUtils.isBlank(inviteData.getSnsaccountid())){
    		CousultInvite tempData = invite.findBySnsaccountidAndOrgi(inviteData.getSnsaccountid() , super.getOrgi(request)) ;
    		if(tempData!=null){
    			tempData.setConsult_vsitorbtn_model(inviteData.getConsult_vsitorbtn_model());
    			tempData.setConsult_vsitorbtn_color(inviteData.getConsult_vsitorbtn_color());
    			tempData.setConsult_vsitorbtn_position(inviteData.getConsult_vsitorbtn_position());
    			tempData.setConsult_vsitorbtn_content(inviteData.getConsult_vsitorbtn_content());
    			tempData.setConsult_vsitorbtn_display(inviteData.getConsult_vsitorbtn_display());
    			tempData.setConsult_dialog_color(inviteData.getConsult_dialog_color());
    			
    			inviteData = tempData ;
    		}
    	}else{
    		inviteData.setSnsaccountid(super.getUser(request).getId());
    	}
    	inviteData.setOrgi(super.getUser(request).getOrgi());
    	if(webimlogo!=null && webimlogo.getOriginalFilename().lastIndexOf(".") > 0){
    		File logoDir = new File(path , "logo");
    		if(!logoDir.exists()){
    			logoDir.mkdirs() ;
    		}
    		String fileName = "logo/"+inviteData.getId()+webimlogo.getOriginalFilename().substring(webimlogo.getOriginalFilename().lastIndexOf(".")) ;
    		FileCopyUtils.copy(webimlogo.getBytes(), new File(path , fileName));
    		inviteData.setConsult_dialog_logo(fileName);
    	}
    	if(agentheadimg!=null && agentheadimg.getOriginalFilename().lastIndexOf(".") > 0){
    		File headimgDir = new File(path , "headimg");
    		if(!headimgDir.exists()){
    			headimgDir.mkdirs() ;
    		}
    		String fileName = "headimg/"+inviteData.getId()+agentheadimg.getOriginalFilename().substring(agentheadimg.getOriginalFilename().lastIndexOf(".")) ;
    		FileCopyUtils.copy(agentheadimg.getBytes(), new File(path , fileName));
    		inviteData.setConsult_dialog_headimg(fileName);
    	}
    	invite.save(inviteData) ;
    	CacheHelper.getSystemCacheBean().put(inviteData.getId(), inviteData, inviteData.getOrgi());
    	
        return request(super.createRequestPageTempletResponse("redirect:/admin/webim/index.html?snsid="+inviteData.getSnsaccountid()));
    }
    
    @RequestMapping("/profile")
    @Menu(type = "webim" , subtype = "profile" , admin= true)
    public ModelAndView profile(ModelMap map , HttpServletRequest request , @Valid String snsid) {
    	CousultInvite coultInvite = invite.findBySnsaccountidAndOrgi(snsid, super.getOrgi(request)) ;
    	if(coultInvite!=null){
    		map.addAttribute("inviteData", coultInvite);
    	}
    	map.addAttribute("import", request.getServerPort()) ;
    	map.addAttribute("snsAccount", snsAccountRes.findBySnsidAndOrgi(snsid, super.getOrgi(request))) ;
        return request(super.createAdminTempletResponse("/admin/webim/profile"));
    }
    
    @RequestMapping("/profile/save")
    @Menu(type = "admin" , subtype = "profile" , admin= true)
    public ModelAndView saveprofile(HttpServletRequest request , @Valid CousultInvite inviteData, @RequestParam(value = "dialogad", required = false) MultipartFile dialogad) throws IOException {
    	CousultInvite tempInviteData  ;
    	if(inviteData!=null && !StringUtils.isBlank(inviteData.getId())){
    		tempInviteData = invite.findOne(inviteData.getId()) ;
    		if(tempInviteData!=null){
    			tempInviteData.setDialog_name(inviteData.getDialog_name());
    			tempInviteData.setDialog_address(inviteData.getDialog_address());
    			tempInviteData.setDialog_phone(inviteData.getDialog_phone());
    			tempInviteData.setDialog_mail(inviteData.getDialog_mail());
    			tempInviteData.setDialog_introduction(inviteData.getDialog_introduction());
    			tempInviteData.setDialog_message(inviteData.getDialog_message());
    			tempInviteData.setLeavemessage(inviteData.isLeavemessage()) ;
    			tempInviteData.setLvmopentype(inviteData.getLvmopentype());
    			tempInviteData.setLvmname(inviteData.isLvmname());
    			tempInviteData.setLvmphone(inviteData.isLvmphone());
    			tempInviteData.setLvmemail(inviteData.isLvmemail());
    			tempInviteData.setLvmaddress(inviteData.isLvmaddress());
    			tempInviteData.setLvmqq(inviteData.isLvmqq());
    			tempInviteData.setSkill(inviteData.isSkill());
    			
    			tempInviteData.setConsult_skill_title(inviteData.getConsult_skill_title());
    			tempInviteData.setConsult_skill_msg(inviteData.getConsult_skill_msg());
    			tempInviteData.setConsult_skill_bottomtitle(inviteData.getConsult_skill_bottomtitle());
    			tempInviteData.setConsult_skill_maxagent(inviteData.getConsult_skill_maxagent());
    			tempInviteData.setConsult_skill_numbers(inviteData.getConsult_skill_numbers());
    			tempInviteData.setConsult_skill_agent(inviteData.isConsult_skill_agent());
    			
    			tempInviteData.setConsult_info(inviteData.isConsult_info());
    			tempInviteData.setConsult_info_email(inviteData.isConsult_info_email());
    			tempInviteData.setConsult_info_name(inviteData.isConsult_info_name());
    			tempInviteData.setConsult_info_phone(inviteData.isConsult_info_phone());
    			tempInviteData.setConsult_info_resion(inviteData.isConsult_info_resion());
    			tempInviteData.setConsult_info_message(inviteData.getConsult_info_message());
    			tempInviteData.setConsult_info_cookies(inviteData.isConsult_info_cookies());
    			
    			tempInviteData.setRecordhis(inviteData.isRecordhis());
    			tempInviteData.setTraceuser(inviteData.isTraceuser());
    			
    			
    			tempInviteData.setAi(inviteData.isAi());
    			tempInviteData.setAifirst(inviteData.isAifirst());
    			tempInviteData.setAimsg(inviteData.getAimsg());
    			tempInviteData.setAisuccesstip(inviteData.getAisuccesstip());
    			tempInviteData.setAiname(inviteData.getAiname());
    			
    			if(dialogad!=null && !StringUtils.isBlank(dialogad.getName()) && dialogad.getBytes()!=null && dialogad.getBytes().length >0){
	    			String fileName = "ad/"+inviteData.getId()+dialogad.getOriginalFilename().substring(dialogad.getOriginalFilename().lastIndexOf(".")) ;
	    			File file = new File(path , fileName) ;
	    			if(!file.getParentFile().exists()){
	    				file.getParentFile().mkdirs();
	    			}
	        		FileCopyUtils.copy(dialogad.getBytes(), file);
	        		tempInviteData.setDialog_ad(fileName);
    			}
        		invite.save(tempInviteData) ;
        		inviteData = tempInviteData ;
    		}
    	}else{
    		invite.save(inviteData) ;
    	}
    	CacheHelper.getSystemCacheBean().put(inviteData.getId(), inviteData, inviteData.getOrgi());
        return request(super.createRequestPageTempletResponse("redirect:/admin/webim/profile.html?snsid="+inviteData.getSnsaccountid()));
    }
    
    @RequestMapping("/invote")
    @Menu(type = "webim" , subtype = "invote" , admin= true)
    public ModelAndView invote(ModelMap map , HttpServletRequest request , @Valid String snsid) {
    	CousultInvite coultInvite = invite.findBySnsaccountidAndOrgi(snsid, super.getOrgi(request)) ;
    	if(coultInvite!=null){
    		map.addAttribute("inviteData", coultInvite);
    	}
    	map.addAttribute("import", request.getServerPort()) ;
    	map.addAttribute("snsAccount", snsAccountRes.findBySnsidAndOrgi(snsid, super.getOrgi(request))) ;
        return request(super.createAdminTempletResponse("/admin/webim/invote"));
    }
    
    @RequestMapping("/invote/save")
    @Menu(type = "admin" , subtype = "profile" , admin= true)
    public ModelAndView saveinvote(HttpServletRequest request , @Valid CousultInvite inviteData, @RequestParam(value = "invotebg", required = false) MultipartFile invotebg) throws IOException {
    	CousultInvite tempInviteData  ;
    	if(inviteData!=null && !StringUtils.isBlank(inviteData.getId())){
    		tempInviteData = invite.findOne(inviteData.getId()) ;
    		if(tempInviteData!=null){
    			tempInviteData.setConsult_invite_enable(inviteData.isConsult_invite_enable());
    			tempInviteData.setConsult_invite_content(inviteData.getConsult_invite_content());
    			tempInviteData.setConsult_invite_accept(inviteData.getConsult_invite_accept());
    			tempInviteData.setConsult_invite_later(inviteData.getConsult_invite_later());
    			tempInviteData.setConsult_invite_delay(inviteData.getConsult_invite_delay());
    			
    			tempInviteData.setConsult_invite_color(inviteData.getConsult_invite_color());
    			
    			if(invotebg!=null && !StringUtils.isBlank(invotebg.getName()) && invotebg.getBytes()!=null && invotebg.getBytes().length >0){
	    			String fileName = "invote/"+inviteData.getId()+invotebg.getOriginalFilename().substring(invotebg.getOriginalFilename().lastIndexOf(".")) ;
	    			File file = new File(path , fileName) ;
	    			if(!file.getParentFile().exists()){
	    				file.getParentFile().mkdirs();
	    			}
	        		FileCopyUtils.copy(invotebg.getBytes(), file);
	        		tempInviteData.setConsult_invite_bg(fileName);
    			}
        		invite.save(tempInviteData) ;
        		inviteData = tempInviteData ;
    		}
    	}else{
    		invite.save(inviteData) ;
    	}
    	CacheHelper.getSystemCacheBean().put(inviteData.getId(), inviteData, inviteData.getOrgi());
        return request(super.createRequestPageTempletResponse("redirect:/admin/webim/invote.html?snsid="+inviteData.getSnsaccountid()));
    }
}