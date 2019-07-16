package net.onebean.uag.conf.service.impl;import com.alibaba.fastjson.JSON;import com.alibaba.fastjson.serializer.SerializerFeature;import net.onebean.common.exception.BusinessException;import net.onebean.common.model.only.serializer.json.BasePaginationResponse;import net.onebean.server.mngt.api.model.UpSteamSyncNodeVo;import net.onebean.server.mngt.api.service.UpSteamInfoApi;import net.onebean.uag.conf.common.ConfPathHelper;import net.onebean.uag.conf.common.ErrorCodesEnum;import net.onebean.uag.conf.service.ManualUpdateServerNodeService;import net.onebean.uag.conf.service.UpgradeNginxConfService;import net.onebean.uag.conf.vo.ConfResult;import net.onebean.util.CollectionUtil;import net.onebean.util.FreeMarkerTemplateUtils;import net.onebean.util.IOUtils;import freemarker.template.Template;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.stereotype.Service;import java.io.*;import java.nio.file.Paths;import java.util.*;@Servicepublic class ManualUpdateServerNodeServiceImpl implements ManualUpdateServerNodeService {    private final static Logger logger = LoggerFactory.getLogger(ManualUpdateServerNodeServiceImpl.class);    private final static String CHARSET_STR ="utf-8";    private final static Boolean IS_SYNC_UPDATE_NGINX = false;    @Autowired    private UpgradeNginxConfService upgradeNginxConfService;    @Autowired    private UpSteamInfoApi upSteamInfoApi;    @Override    public Boolean updateAllNginxUpSteamConf() {        /*查询可用的upsteam节点 生成upsteam.conf文件*/        ConfResult confResult = generateUpstream(this.getAllUpSteamNode());        /*异步更新nginx节点配置*/        upgradeNginxConfService.updateAllRemoteNginxConf(confResult.getCoverFiles(), confResult.getRemoveFiles(), IS_SYNC_UPDATE_NGINX);        return true;    }    @Override    public ConfResult generateUpstream(List<UpSteamSyncNodeVo> upSteamNodeVos) {        logger.info("生成upstream.conf文件：" + JSON.toJSONString(upSteamNodeVos, SerializerFeature.WriteMapNullValue));        /*在本地创建目录*/        IOUtils.mkDir(ConfPathHelper.getLocalConfDir());        /*生成对应内容 并写成文件*/        ConfResult confResult = new ConfResult();        Map<String, List<UpSteamSyncNodeVo>> data = new HashMap<>();        data.put("marathons", upSteamNodeVos);        String localUpstreamPath = Paths.get(ConfPathHelper.getLocalBasePath(), "conf.d", "upstream.conf").toFile().getAbsolutePath();        File mapperFile = new File(localUpstreamPath);        FileOutputStream fos = null;        Writer out = null;        try {            fos = new FileOutputStream(mapperFile);            out = new BufferedWriter(new OutputStreamWriter(fos, CHARSET_STR),10240);            Template template = FreeMarkerTemplateUtils.getTemplate("/upstream/upstream.ftl");            template.process(data,out);        } catch (Exception e) {            logger.error("generateUpstream error e = ",e);            throw new BusinessException(ErrorCodesEnum.IO_ERROR.code(),ErrorCodesEnum.IO_ERROR.msg()+" generate Upstream on freeMarker");        }finally {            try {                fos.close();                out.close();            } catch (IOException e) {                e.printStackTrace();            }        }        /*返回生成的文件记录*/        confResult.addCoverFile(ConfPathHelper.getEcsRelativePath(localUpstreamPath));        return confResult;    }    @SuppressWarnings("unchecked")    @Override    public List<UpSteamSyncNodeVo> getAllUpSteamNode() {        BasePaginationResponse<UpSteamSyncNodeVo> resp = upSteamInfoApi.findSyncUpSteamNode();        List<UpSteamSyncNodeVo> list = Optional.ofNullable(resp).map(BasePaginationResponse::getDatas).orElse(null);        if (CollectionUtil.isNotEmpty(list)){            return list;        }else{            logger.warn("getAllUpSteamNode findSyncUpSteamNode res is empty");            return Collections.EMPTY_LIST;        }    }}