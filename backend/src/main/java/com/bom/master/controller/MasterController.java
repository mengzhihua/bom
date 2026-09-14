package com.bom.master.controller;
import com.bom.common.*; import com.bom.master.entity.*; import com.bom.master.mapper.*; import com.bom.process.entity.WorkStation; import com.bom.process.mapper.WorkStationMapper; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/master") @RequiredArgsConstructor
public class MasterController {
 private final VehicleModelMapper models; private final PlantMapper plants; private final SupplierMapper suppliers; private final FeatureMapper features; private final FeatureOptionMapper options; private final WorkStationMapper stations;
 @GetMapping("/vehicle-models") public R<List<VehicleModel>> models(){return R.ok(models.selectList(null));}
 @PostMapping("/vehicle-models") public R<VehicleModel> model(@RequestBody VehicleModel x){x.setId(null);models.insert(x);return R.ok(x);}
 @GetMapping("/plants") public R<List<Plant>> plants(){return R.ok(plants.selectList(null));}
 @PostMapping("/plants") public R<Plant> plant(@RequestBody Plant x){x.setId(null);plants.insert(x);return R.ok(x);}
 @GetMapping("/suppliers") public R<List<Supplier>> suppliers(){return R.ok(suppliers.selectList(null));}
 @PostMapping("/suppliers") public R<Supplier> supplier(@RequestBody Supplier x){x.setId(null);suppliers.insert(x);return R.ok(x);}
 @GetMapping("/features") public R<List<Feature>> features(){return R.ok(features.selectList(null));}
 @GetMapping("/features/{id}/options") public R<List<FeatureOption>> options(@PathVariable Long id){return R.ok(options.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FeatureOption>().eq("feature_id",id)));}
 @PostMapping("/features") public R<Feature> feature(@RequestBody Feature x){x.setId(null);features.insert(x);return R.ok(x);}
 @PostMapping("/features/{id}/options") public R<FeatureOption> option(@PathVariable Long id,@RequestBody FeatureOption x){x.setId(null);x.setFeatureId(id);options.insert(x);return R.ok(x);}
 @GetMapping("/workstations") public R<List<WorkStation>> stations(){return R.ok(stations.selectList(null));}
 @PostMapping("/workstations") public R<WorkStation> station(@RequestBody WorkStation x){x.setId(null);stations.insert(x);return R.ok(x);}
}
