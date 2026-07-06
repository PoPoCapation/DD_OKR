package cn.bugstack.trigger.http;

import cn.bugstack.api.dto.LoginRequestDTO;
import cn.bugstack.api.dto.LoginResponseDTO;
import cn.bugstack.api.dto.RegisterRequestDTO;
import cn.bugstack.api.response.Response;
import cn.bugstack.cases.auth.ILoginCase;
import cn.bugstack.cases.auth.IRegisterCase;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class AuthController {

    @Resource
    private ILoginCase loginCase;
    @Resource
    private IRegisterCase registerCase;

    @PostMapping("/login")
    public Response<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        if (request == null || request.getAccount() == null || request.getPassword() == null) {
            return Response.<LoginResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }
        LoginResultVO result = loginCase.login(request.getAccount(), request.getPassword());
        return toResponse(result);
    }

    @PostMapping("/register")
    public Response<LoginResponseDTO> register(@RequestBody RegisterRequestDTO request) {
        if (request == null || request.getAccount() == null || request.getPassword() == null) {
            return Response.<LoginResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }
        LoginResultVO result = registerCase.register(request.getAccount(), request.getPassword(), request.getUsername());
        return toResponse(result);
    }

    /** 登录/注册结果统一包装 */
    private Response<LoginResponseDTO> toResponse(LoginResultVO result) {
        LoginResponseDTO data = LoginResponseDTO.builder()
                .userId(result.getUserId())
                .account(result.getAccount())
                .token(result.getToken())
                .roles(result.getRoles())
                .permissions(result.getPermissions())
                .build();
        return Response.<LoginResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(data)
                .build();
    }
}
