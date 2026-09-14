package com.bom.process.service;

import com.bom.process.entity.WorkStation;
import com.bom.process.mapper.WorkStationMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkStationService {

    private final WorkStationMapper stations;

    public List<WorkStation> list() {
        return stations.selectList(null);
    }

    public WorkStation create(WorkStation station) {
        station.setId(null);
        stations.insert(station);
        return station;
    }

    public WorkStation update(Long id, WorkStation station) {
        station.setId(id);
        stations.updateById(station);
        return stations.selectById(id);
    }
}
