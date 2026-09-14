package com.bom.master.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.common.R;
import com.bom.master.entity.Feature;
import com.bom.master.entity.FeatureOption;
import com.bom.master.entity.Plant;
import com.bom.master.entity.Supplier;
import com.bom.master.entity.VehicleModel;
import com.bom.master.mapper.FeatureMapper;
import com.bom.master.mapper.FeatureOptionMapper;
import com.bom.master.mapper.PlantMapper;
import com.bom.master.mapper.SupplierMapper;
import com.bom.master.mapper.VehicleModelMapper;
import com.bom.process.entity.WorkStation;
import com.bom.process.service.WorkStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/master")
@RequiredArgsConstructor
public class MasterController {

    private final VehicleModelMapper models;
    private final PlantMapper plants;
    private final SupplierMapper suppliers;
    private final FeatureMapper features;
    private final FeatureOptionMapper options;
    private final WorkStationService stationService;

    @GetMapping("/vehicle-models")
    public R<List<VehicleModel>> models() {
        return R.ok(models.selectList(null));
    }

    @PostMapping("/vehicle-models")
    public R<VehicleModel> model(@RequestBody VehicleModel model) {
        model.setId(null);
        models.insert(model);
        return R.ok(model);
    }

    @PutMapping("/vehicle-models/{id}")
    public R<VehicleModel> updateModel(
            @PathVariable Long id,
            @RequestBody VehicleModel model) {
        model.setId(id);
        models.updateById(model);
        return R.ok(models.selectById(id));
    }

    @GetMapping("/plants")
    public R<List<Plant>> plants() {
        return R.ok(plants.selectList(null));
    }

    @PostMapping("/plants")
    public R<Plant> plant(@RequestBody Plant plant) {
        plant.setId(null);
        plants.insert(plant);
        return R.ok(plant);
    }

    @PutMapping("/plants/{id}")
    public R<Plant> updatePlant(
            @PathVariable Long id,
            @RequestBody Plant plant) {
        plant.setId(id);
        plants.updateById(plant);
        return R.ok(plants.selectById(id));
    }

    @GetMapping("/suppliers")
    public R<List<Supplier>> suppliers() {
        return R.ok(suppliers.selectList(null));
    }

    @PostMapping("/suppliers")
    public R<Supplier> supplier(@RequestBody Supplier supplier) {
        supplier.setId(null);
        suppliers.insert(supplier);
        return R.ok(supplier);
    }

    @PutMapping("/suppliers/{id}")
    public R<Supplier> updateSupplier(
            @PathVariable Long id,
            @RequestBody Supplier supplier) {
        supplier.setId(id);
        suppliers.updateById(supplier);
        return R.ok(suppliers.selectById(id));
    }

    @GetMapping("/features")
    public R<List<Feature>> features() {
        return R.ok(features.selectList(null));
    }

    @GetMapping("/features/{id}/options")
    public R<List<FeatureOption>> options(@PathVariable Long id) {
        return R.ok(options.selectList(new QueryWrapper<FeatureOption>()
                .eq("feature_id", id)));
    }

    @PostMapping("/features")
    public R<Feature> feature(@RequestBody Feature feature) {
        feature.setId(null);
        features.insert(feature);
        return R.ok(feature);
    }

    @PutMapping("/features/{id}")
    public R<Feature> updateFeature(
            @PathVariable Long id,
            @RequestBody Feature feature) {
        feature.setId(id);
        features.updateById(feature);
        return R.ok(features.selectById(id));
    }

    @PostMapping("/features/{id}/options")
    public R<FeatureOption> option(
            @PathVariable Long id,
            @RequestBody FeatureOption option) {
        option.setId(null);
        option.setFeatureId(id);
        options.insert(option);
        return R.ok(option);
    }

    @GetMapping("/workstations")
    public R<List<WorkStation>> workstations() {
        return R.ok(stationService.list());
    }

    @PostMapping("/workstations")
    public R<WorkStation> workstation(@RequestBody WorkStation workstation) {
        return R.ok(stationService.create(workstation));
    }

    @PutMapping("/workstations/{id}")
    public R<WorkStation> updateWorkstation(
            @PathVariable Long id,
            @RequestBody WorkStation workstation) {
        return R.ok(stationService.update(id, workstation));
    }
}
