package com.jasolar.mis.module.system.controller.admin.outer.resp;

import lombok.Data;

/**
 * Description:
 * Author : Zhou Hai
 * Date : 06/08/2025 17:15
 * Version : 1.0
 */
@Data
public class SynResp {

    private EsbInfoResp esbInfo;

    private ResultInfoResp resultInfo;



    public static SynResp success(String message,EsbInfoResp esbInfo){


        ResultInfoResp resp = new ResultInfoResp();
        resp.setCode(200);
        resp.setMessage(message);

        SynResp synResp = new SynResp();
        synResp.setEsbInfo(esbInfo);
        synResp.setResultInfo(resp);

        return synResp;

    }


    public static SynResp failed(String message,EsbInfoResp esbInfo){
        ResultInfoResp resp = new ResultInfoResp();
        resp.setCode(500);
        resp.setMessage(message);

        SynResp synResp = new SynResp();
        synResp.setEsbInfo(esbInfo);
        synResp.setResultInfo(resp);
        return synResp;
    }





}
