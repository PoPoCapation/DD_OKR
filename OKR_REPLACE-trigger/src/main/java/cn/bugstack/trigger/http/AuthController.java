package cn.bugstack.trigger.http;

import cn.bugstack.api.IAuthController;
import cn.bugstack.api.dto.LoginRequestDTO;
import cn.bugstack.api.dto.LoginResponseDTO;
import cn.bugstack.api.response.Response;
import cn.bugstack.domain.user.model.valobj.LoginResultVO;
import cn.bugstack.domain.user.service.IAuthService;
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
public class AuthController implements IAuthController {

    @Resource
    private IAuthService authService;

    @PostMapping("/login")
    public Response<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        if (request == null || request.getAccount() == null || request.getPassword() == null) {
            return Response.<LoginResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }
        LoginResultVO result = authService.login(request.getAccount(), request.getPassword());
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
