package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/interceptors")
@Validated
@LogExecution
public class InterceptorController {

    private final InterceptorFacade interceptorFacade;

    public InterceptorController(InterceptorFacade interceptorFacade) {
        this.interceptorFacade = interceptorFacade;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<InterceptorDto> getAll(HttpServletResponse response) {
        return interceptorFacade.getAllInterceptors();
    }

    @GetMapping(path = "/{interceptorName}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public InterceptorDto getInterceptor(HttpServletResponse response,
                                         @PathVariable("interceptorName") String interceptorName) {
        return interceptorFacade.getInterceptor(interceptorName);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createInterceptor(HttpServletResponse response,
                                  @RequestBody @Valid InterceptorDto interceptorDto) {
        interceptorFacade.createInterceptor(interceptorDto);
    }

    @PutMapping(path = "/{interceptorName}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateInterceptor(HttpServletResponse response,
                                  @PathVariable("interceptorName") String interceptorName,
                                  @RequestBody @Valid InterceptorDto interceptorDto) {
        interceptorFacade.updateInterceptor(interceptorName, interceptorDto);
    }

    @DeleteMapping(path = "/{interceptorName}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInterceptor(HttpServletResponse response,
                                  @PathVariable("interceptorName") String interceptorName) {
        interceptorFacade.deleteInterceptor(interceptorName);
    }


    @GetMapping(path = "/{interceptorName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public InterceptorDto getSnapshot(@PathVariable String interceptorName, @PathVariable Integer revision) {
        return interceptorFacade.getSnapshot(interceptorName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<InterceptorDto> getAllAtRevision(HttpServletResponse response, @PathVariable Integer revision) throws Exception {
        return interceptorFacade.getAllAtRevision(revision);
    }
}
