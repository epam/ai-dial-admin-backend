package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.dto.RouteDto;
import com.epam.aidial.cfg.web.facade.RouteFacade;
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
@RequestMapping("/api/v1/routes")
@Validated
@LogExecution
public class RouteController {

    private final RouteFacade routeFacade;

    public RouteController(RouteFacade routeFacade) {
        this.routeFacade = routeFacade;
    }

    @GetMapping(produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<RouteDto> getAllRoutes(HttpServletResponse response) {
        return routeFacade.getAllRoutes();
    }

    @GetMapping(path = "/{routeName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RouteDto getRoute(HttpServletResponse response,
                             @PathVariable("routeName") String routeName) {
        return routeFacade.getRoute(routeName);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createRoute(HttpServletResponse response,
                            @RequestBody @Valid RouteDto routeDto) {
        routeFacade.createRoute(routeDto);
    }

    @PutMapping(path = "/{routeName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRoute(HttpServletResponse response,
                            @PathVariable("routeName") String routeName,
                            @RequestBody @Valid RouteDto routeDto) {
        routeFacade.updateRoute(routeName, routeDto);
    }

    @DeleteMapping(path = "/{routeName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoute(HttpServletResponse response,
                            @PathVariable("routeName") String routeName) {
        routeFacade.deleteRoute(routeName);
    }

    @GetMapping(path = "/{routeName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RouteDto getSnapshot(@PathVariable String routeName, @PathVariable Integer revision) {
        return routeFacade.getSnapshot(routeName, revision);
    }
}
