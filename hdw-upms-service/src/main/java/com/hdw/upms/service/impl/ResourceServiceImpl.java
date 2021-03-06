package com.hdw.upms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.hdw.common.result.MenuNode;
import com.hdw.common.result.ZTreeNode;
import com.hdw.upms.entity.Resource;
import com.hdw.upms.entity.vo.RoleVo;
import com.hdw.upms.entity.vo.UserVo;
import com.hdw.upms.mapper.ResourceMapper;
import com.hdw.upms.mapper.RoleMapper;
import com.hdw.upms.mapper.RoleResourceMapper;
import com.hdw.upms.mapper.UserRoleMapper;
import com.hdw.upms.service.IResourceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 *
 * Resource 表数据服务层接口实现类
 *
 */
@Service(
        version = "1.0.0",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}",
        group = "hdw-upms"
)
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements IResourceService {
    private static final int RESOURCE_MENU = 0; // 菜单

    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private RoleResourceMapper roleResourceMapper;
    
    @Override
    public List<ZTreeNode> selectTree() {
    	List<ZTreeNode> trees=new ArrayList<>();
        EntityWrapper<Resource> wrapper = new EntityWrapper<Resource>();
        wrapper.orderBy("seq");
        List<Resource> list=resourceMapper.selectList(wrapper);
        if(list!=null && !list.isEmpty()) {
        	for (Resource resource : list) {
				ZTreeNode tree=new ZTreeNode();
				tree.setId(resource.getId());
				tree.setpId(resource.getPid());
				tree.setName(resource.getName());
				tree.setIsOpen(true);
				trees.add(tree);
			}
        }
        return trees;
        		     		
    }
    
    public List<Resource> selectByType(Integer type) {
        EntityWrapper<Resource> wrapper = new EntityWrapper<Resource>();
        Resource resource = new Resource();
        wrapper.setEntity(resource);
        wrapper.addFilter("resource_type = {0}", type);
        wrapper.orderBy("seq");
        return resourceMapper.selectList(wrapper);
    }
    
    @Override
    public List<MenuNode> selectMenu() {
        List<MenuNode> trees = new ArrayList<MenuNode>();
        // 查询所有菜单
        List<Resource> resources = this.selectByType(RESOURCE_MENU);
        if (resources == null) {
            return trees;
        }
        for (Resource resource : resources) {
        	MenuNode tree = new MenuNode();
            tree.setId(resource.getId());
            tree.setPid(resource.getPid());
            tree.setIcon(resource.getIcon());
            tree.setName(resource.getName());
            tree.setUrl(resource.getUrl());
            tree.setNum(resource.getSeq());
            tree.setIsmenu(resource.getResourceType());
            tree.setStatus(resource.getStatus());
            trees.add(tree);
        }
        return trees;
    }
    
    /**
     * 
     * @param type 菜单类型
     * @param pid 上级菜单Id
     * @return
     */
    public List<Resource> selectByTypeAndPid(Integer type,Long pid) {
        EntityWrapper<Resource> wrapper = new EntityWrapper<Resource>();
        Resource resource = new Resource();
        wrapper.setEntity(resource);
        wrapper.addFilter("resource_type = {0}", type);
        wrapper.addFilter("pid = {0}", pid);
        wrapper.orderBy("seq");
        return resourceMapper.selectList(wrapper);
    }

	@Override
    public List<MenuNode> selectTree(UserVo userVo) {
    	List<MenuNode> trees = new ArrayList<MenuNode>();
        // shiro中缓存的用户角色
        Set<String> roles=new HashSet<>();
        for (RoleVo roleVo:userVo.getRolesList()){
            roles.add(roleVo.getName());
        }
        if (roles.isEmpty()) {
            return trees;
        }
        // 如果有超级管理员权限
        if (roles.contains("admin")) {
            List<Resource> resourceList = this.selectByTypeAndPid(RESOURCE_MENU, 0l);
            if (resourceList == null) {
                return trees;
            }
            for (Resource resource : resourceList) {
            	MenuNode tree = new MenuNode();
                tree.setId(resource.getId());
                tree.setPid(resource.getPid());
                tree.setName(resource.getName());
                tree.setIcon(resource.getIcon());
                tree.setUrl(resource.getUrl());
                tree.setStatus(resource.getStatus());
                tree.setNum(resource.getSeq());
                List<MenuNode> treeTwo=new ArrayList<MenuNode>();
                List<Resource> resourceTwo=this.selectByTypeAndPid(RESOURCE_MENU, resource.getId());
                if(resourceTwo!=null&&!resourceTwo.isEmpty()) {
                	for (Resource resource2 : resourceTwo) {
                		MenuNode tree2 = new MenuNode();
                        tree2.setId(resource2.getId());
                        tree2.setPid(resource2.getPid());
                        tree2.setName(resource2.getName());
                        tree2.setIcon(resource2.getIcon());
                        tree2.setUrl(resource2.getUrl());
                        tree2.setStatus(resource2.getStatus());
                        tree2.setNum(resource2.getSeq());
                        
                        List<MenuNode> treeThree=new ArrayList<MenuNode>();
                        List<Resource> resourceThree=this.selectByTypeAndPid(RESOURCE_MENU, resource2.getId());
                        if(resourceThree!=null&&!resourceThree.isEmpty()) {
                        	for (Resource resource3 : resourceThree) {
                        		MenuNode tree3 = new MenuNode();
                                tree3.setId(resource3.getId());
                                tree3.setPid(resource3.getPid());
                                tree3.setName(resource3.getName());
                                tree3.setIcon(resource3.getIcon());
                                tree3.setUrl(resource3.getUrl());
                                tree3.setStatus(resource3.getStatus());
                                tree3.setNum(resource3.getSeq());
                                treeThree.add(tree3);
        					}
                        }
                        tree2.setChildren(treeThree);
                        treeTwo.add(tree2);
					}
                }
                tree.setChildren(treeTwo);
                trees.add(tree);
            }
            return trees;
        }
        // 普通用户
        List<Long> roleIdList = userRoleMapper.selectRoleIdListByUserId(userVo.getId());
        if (roleIdList == null) {
            return trees;
        }
        List<Resource> resourceLists = roleMapper.selectResourceListByRoleIdAndPidList(roleIdList,0l);
        if (resourceLists == null) {
            return trees;
        }
        for (Resource resource : resourceLists) {
        	MenuNode tree = new MenuNode();
            tree.setId(resource.getId());
            tree.setPid(resource.getPid());
            tree.setName(resource.getName());
            tree.setIcon(resource.getIcon());
            tree.setUrl(resource.getUrl());
            tree.setNum(resource.getSeq());
            tree.setStatus(resource.getStatus());
            List<MenuNode> treeTwo=new ArrayList<MenuNode>();
            List<Resource> resourceTwo=roleMapper.selectResourceListByRoleIdAndPidList(roleIdList,resource.getId());
            if(resourceTwo!=null&&!resourceTwo.isEmpty()) {
            	for (Resource resource2 : resourceTwo) {
            		MenuNode tree2 = new MenuNode();
                    tree2.setId(resource2.getId());
                    tree2.setPid(resource2.getPid());
                    tree2.setName(resource2.getName());
                    tree2.setIcon(resource2.getIcon());
                    tree2.setUrl(resource2.getUrl());
                    tree2.setNum(resource2.getSeq());
                    tree2.setStatus(resource2.getStatus());
                    
                    List<MenuNode> treeThree=new ArrayList<MenuNode>();
                    List<Resource> resourceThree=roleMapper.selectResourceListByRoleIdAndPidList(roleIdList,resource2.getId());
                    if(resourceThree!=null&&!resourceThree.isEmpty()) {
                    	for (Resource resource3 : resourceThree) {
                    		MenuNode tree3 = new MenuNode();
                            tree3.setId(resource3.getId());
                            tree3.setPid(resource3.getPid());
                            tree3.setName(resource3.getName());
                            tree3.setIcon(resource3.getIcon());
                            tree3.setUrl(resource3.getUrl());
                            tree3.setNum(resource3.getSeq());
                            tree3.setStatus(resource3.getStatus());
                            treeThree.add(tree3);
    					}
                    }
                    tree2.setChildren(treeThree);
                    treeTwo.add(tree2);
				}
            }
            tree.setChildren(treeTwo);
            trees.add(tree);
            
        }
        return trees;
    }

	@Override
	public boolean deleteById(Serializable resourceId) {
		roleResourceMapper.deleteByResourceId(resourceId);
		return super.deleteById(resourceId);
	}

	@Override
	public List<Map<String, Object>> selectTreeGrid(Map<String, Object> par) {
		
		return resourceMapper.selectTreeGrid(par);
	}

	@Override
	public List<ZTreeNode> selectMenuTree() {
		return resourceMapper.selectMenuTree();
	}
}