package com.fh.scms.controllers.admin;

import com.fh.scms.dto.MessageResponse;
import com.fh.scms.pojo.Tax;
import com.fh.scms.services.TaxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/admin/taxs", produces = "application/json; charset=UTF-8")
public class TaxController {

    private final TaxService taxService;

    @GetMapping
    public String listTax(Model model, @RequestParam(required = false, defaultValue = "") Map<String, String> params) {
        model.addAttribute("taxes", this.taxService.findAllWithFilter(params));

        return "taxes";
    }

    @GetMapping(path = "/add")
    public String addTax(Model model) {
        model.addAttribute("tax", new Tax());

        return "add_tax";
    }

    @PostMapping(path = "/add")
    public String addTax(Model model, @ModelAttribute(value = "tax") @Valid Tax tax, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<MessageResponse> errors = MessageResponse.fromBindingResult(bindingResult);
            model.addAttribute("errors", errors);

            return "add_tax";
        }

        this.taxService.save(tax);

        return "redirect:/admin/taxs";
    }

    @GetMapping(path = "/edit/{taxId}")
    public String editTax(Model model, @PathVariable(value = "taxId") Long id) {
        model.addAttribute("tax", this.taxService.findById(id));

        return "edit_tax";
    }

    @PostMapping(path = "/edit/{taxId}")
    public String editTax(Model model, @PathVariable(value = "taxId") Long id,
                          @ModelAttribute(value = "tax") @Valid Tax tax, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<MessageResponse> errors = MessageResponse.fromBindingResult(bindingResult);
            model.addAttribute("errors", errors);

            return "edit_tax";
        }

        this.taxService.update(tax);

        return "redirect:/admin/taxs";
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/delete/{taxId}")
    public String deleteTax(@PathVariable(value = "taxId") Long id) {
        this.taxService.delete(id);

        return "redirect:/admin/taxs";
    }
}
