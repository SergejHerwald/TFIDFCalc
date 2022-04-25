import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class FileuploaderService {

  constructor(private http: HttpClient) { }

  apiPath:string = "http://localhost:8080"

  upload(file:File,name:string):Observable<{uploaded: boolean}> {
    const formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post<{uploaded: boolean}>(`${this.apiPath}/uploadFile`, formData)
  }
}
