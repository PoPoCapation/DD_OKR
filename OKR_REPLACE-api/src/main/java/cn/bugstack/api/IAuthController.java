package cn.bugstack.api;

import cn.bugstack.api.dto.LoginRequestDTO;
import cn.bugstack.api.dto.LoginResponseDTO;
import cn.bugstack.api.response.Response;

public interface IAuthController {
    Response<LoginResponseDTO> login( LoginRequestDTO request);
}
