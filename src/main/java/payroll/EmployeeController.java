package payroll;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
class EmployeeController {
	private final EmployeeRepository repository;
	private final EmployeeModelAssembler assembler;

	EmployeeController(EmployeeRepository repository, EmployeeModelAssembler assembler){
		this.repository = repository;
		this.assembler = assembler;
	}

	// Aggregate root
	// tag::get-aggregate
	@GetMapping("/employees")
	CollectionModel<EntityModel<Employee>> all() {

		List<EntityModel<Employee>> employees = repository.findAll().stream()
		.map(assembler:: toModel)
		.collect(Collectors.toList());

		return CollectionModel.of(employees, linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
	}

	// end::get-aggregate-root[]

	@PostMapping("/employees")
	ResponseEntity<?> newEmployee(@RequestBody Employee newEmployee){
		Employee newSaveRecord = repository.save(newEmployee);
		EntityModel<Employee> entityModel = assembler.toModel(newSaveRecord);
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
		.body(entityModel);
	}

	@GetMapping("/employees/{id}")
	EntityModel<Employee> one(@PathVariable Long id){

		Employee employee = repository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));

		return assembler.toModel(employee);
	}

	@PutMapping("/employees/{id}")
	ResponseEntity<?> replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id){
		Employee updatedEmployee = repository.findById(id)
		.map((employee) -> {
			employee.setName(newEmployee.getName());
			employee.setRole(newEmployee.getRole());

			return repository.save(employee);
		})
		.orElseGet(() -> {
			return repository.save(newEmployee);
		});

		EntityModel<Employee> updaEntityModel = assembler.toModel(updatedEmployee);

		return ResponseEntity.created(updaEntityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
		.body(updaEntityModel);
	}

	@DeleteMapping("/employees/{id}")
	ResponseEntity<?> deleteEmployee(@PathVariable long id){
		repository.deleteById(id);

		return ResponseEntity.noContent().build();
	}
	
}

