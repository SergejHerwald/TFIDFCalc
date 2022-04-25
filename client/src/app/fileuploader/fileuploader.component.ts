import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {FileuploaderService} from "./fileuploader.service";

@Component({
  selector: 'app-fileuploader',
  templateUrl: './fileuploader.component.html',
  styleUrls: ['./fileuploader.component.css']
})
export class FileuploaderComponent implements OnInit {

  constructor(private fileuploaderService: FileuploaderService) { }


  form: FormGroup;
  file: File;

  ngOnInit(): void {
    this.form = new FormGroup({
      name: new FormControl('',[Validators.minLength(6), Validators.required]),
      file: new FormControl('', [Validators.required])

    })
  }



  onUpload() {
    this.fileuploaderService.upload(this.file, this.form.value.name).subscribe(()=>{

    })
  }

  onSelected(event:Event) {
    const target = event.target as HTMLInputElement;
    const files = target.files as FileList;
    this.file = files[0]
  }
}
