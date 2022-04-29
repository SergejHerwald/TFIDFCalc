import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {MatSnackBar} from "@angular/material/snack-bar";
import {FileuploaderService} from "./fileuploader.service";

@Component({
  selector: 'app-fileuploader',
  templateUrl: './fileuploader.component.html',
  styleUrls: ['./fileuploader.component.css']
})
export class FileuploaderComponent implements OnInit {

  constructor(private fileuploaderService: FileuploaderService,
    private _snackBar: MatSnackBar) { }

  apiPath:string = "http://localhost:8080"

  uploadform: FormGroup;
  getCloudform: FormGroup;

  uploadFormValid:boolean;
  getCloudformValid:boolean;
  batchWorkValid:boolean;

  workStarted: boolean = false;
  uploadStarted:boolean = false;
  imageLoaded:boolean = false;

  url: string;

  file: File;
  files:string[];
  fileSelected:string | undefined;

  ngOnInit(): void {
    this.getFiles()
    this.uploadform = new FormGroup({

      file: new FormControl('', [Validators.required])
    })
    this.getCloudform = new FormGroup({
      file: new FormControl('', [Validators.required])
    })
    this.uploadFormValid = this.getCloudform.valid;
    this.getCloudformValid = this.getCloudform.valid;

  }

  onUpload() {
    this.uploadStarted = true;
    this.uploadFormValid = false;
    this.fileuploaderService.upload(this.file).subscribe(()=>{
      this.uploadStarted = false;
      this.uploadFormValid = true;
      this.okPopUp("File uploaded")
    },()=>{
      this.uploadStarted = false;
      this.uploadFormValid = true;
      this.errorPopUp("Something went wrong")
    })
  }

  getFiles(){
    this.fileuploaderService.getFiles().subscribe((files)=>{
      this.files = files
      this.batchWorkValid = this.files.length != 0;
      this.okPopUp("You can choose a file or upload new one")
    },()=>{
      this.errorPopUp("There are no files uploaded")
    })
  }

  onSelected(event:Event) {

    const target = event.target as HTMLInputElement;
    const files = target.files as FileList;
    this.file = files[0]
    if(this.file){
      this.uploadFormValid = true;
    }else{
      this.uploadFormValid = false;
    }

  }

  getImage() {
    this.url = `${this.apiPath}/images/${this.fileSelected}.png`;
    this.imageLoaded = true;
  }

  startBatchWork() {
    this.workStarted = true;
    this.batchWorkValid = false;
    this.okPopUp("Batch job started!");
    this.fileuploaderService.startBatchWork().subscribe(()=>{
      this.workStarted = false;
      this.batchWorkValid = true;
      this.okPopUp("Batch job ended!");
    },()=>{
      this.workStarted = false;
      this.batchWorkValid = true;
      this.errorPopUp("Something went wrong")
    })
  }

  errorPopUp(msg:string){
    this._snackBar.open(msg, "Ok!", {
      duration: 5000,
      panelClass: ['red-snackbar']
    })
  }
  okPopUp(msg:string){
    this._snackBar.open(msg, "Ok!", {
      duration: 5000,
      panelClass: ['green-snackbar']
    })
  }



}
