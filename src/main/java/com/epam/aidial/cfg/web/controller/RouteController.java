package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.web.facade.RouteFacade;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ResponseEntity<RouteDto> getRoute(@PathVariable("routeName") String routeName,
                                             @RequestHeader(value = "If-None-Match") String previousHash) {
        var dtoWithHash = routeFacade.getRouteWithHash(routeName);
        return dtoWithHash.hash().equals(StringUtils.unwrap(previousHash, '"'))
                ? ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(dtoWithHash.hash()).build()
                : ResponseEntity.status(HttpStatus.OK).eTag(dtoWithHash.hash()).body(dtoWithHash.dto());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createRoute(@RequestBody @Valid RouteDto routeDto) {
        routeFacade.createRoute(routeDto);
    }

    @PutMapping(path = "/{routeName}",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateRoute(@PathVariable("routeName") String routeName,
                                            @RequestBody @Valid RouteDto routeDto,
                                            @RequestHeader(value = "If-Match") String previousHash) {
        var newHash = routeFacade.updateRoute(routeName, routeDto, StringUtils.unwrap(previousHash, '"'));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).eTag(newHash).build();
    }

    @DeleteMapping(path = "/{routeName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoute(@PathVariable("routeName") String routeName) {
        routeFacade.deleteRoute(routeName);
    }

    @GetMapping(path = "/{routeName}/revision/{revision}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public RouteDto getSnapshot(@PathVariable String routeName, @PathVariable Integer revision) {
        return routeFacade.getSnapshot(routeName, revision);
    }

    @GetMapping(path = "/revision/{revision}", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public Collection<RouteDto> getAllAtRevision(@PathVariable Integer revision) throws Exception {
        return routeFacade.getAllAtRevision(revision);
    }
}
